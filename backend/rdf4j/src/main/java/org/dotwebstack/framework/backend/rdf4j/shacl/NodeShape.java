package org.dotwebstack.framework.backend.rdf4j.shacl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
}
