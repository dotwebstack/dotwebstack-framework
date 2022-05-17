package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.GraphQLFieldDefinition;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

@Builder
@Getter
public final class NodeShape {

  private final Resource identifier;

  /**
   * The entries in the outer set are joined by AND, the entries in the inner set are joined by OR.
   */
  @Builder.Default
  private final Set<Set<IRI>> classes = new HashSet<>();

  private final IRI parent;

  private final String name;

  @Builder.Default
  private final Map<String, PropertyShape> propertyShapes = new HashMap<>();

  public PropertyShape getPropertyShape(String propertyShapeName) {
    var propertyShape = this.propertyShapes.get(propertyShapeName);

    // We need to validate the graphql definition against the shacl definition on bootstrap
    if (propertyShape == null) {
      throw new InvalidConfigurationException("No property shape found for name '{}' nodeshape '{}'", propertyShapeName,
          this.name);
    }

    return propertyShape;
  }

  /*
   * Obtain the nodeshape for the given fieldpath.
   */
  public Optional<NodeShape> getChildNodeShape(List<GraphQLFieldDefinition> fieldPath) {
    return fieldPath.stream()
        .findFirst()
        .map(GraphQLFieldDefinition::getName)
        .map(this::getPropertyShape)
        .map(PropertyShape::getNode)
        .or(() -> {
          if (fieldPath.size() > 1) {
            // this means that we have found a scalar field -> we cannot go any level deeper anymore
            throw illegalArgumentException("Cannot get child shape '{}' from '{}'", fieldPath.stream()
                .map(GraphQLFieldDefinition::getName)
                .collect(Collectors.joining(".")), getIdentifier().stringValue());
          }
          return Optional.empty();
        });
  }
}
