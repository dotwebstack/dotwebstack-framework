package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.schema.GraphQLFieldDefinition;
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

  @Builder.Default
  private final Set<IRI> classes = new HashSet<>();

  private final IRI parent;

  private final String name;

  private final Map<String, PropertyShape> propertyShapes;

  public PropertyShape getPropertyShape(String propertyShapeName) {
    PropertyShape propertyShape = this.propertyShapes.get(propertyShapeName);

    // We need to validate the graphql definition against the shacl definition on bootstrap
    if (propertyShape == null) {
      throw new InvalidConfigurationException("No property shape found for name '{}' nodeshape '{}'", propertyShapeName,
          this.name);
    }

    return propertyShape;
  }

  /*
   * Check out if it is possible to go one level deeper, If not return null, but only if we are
   * certain that this is the last path that we process
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
