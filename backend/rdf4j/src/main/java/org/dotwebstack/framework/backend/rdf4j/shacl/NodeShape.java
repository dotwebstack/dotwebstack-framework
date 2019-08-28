package org.dotwebstack.framework.backend.rdf4j.shacl;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;

@Builder
@Getter
public final class NodeShape {

  private final IRI identifier;

  private final IRI targetClass;

  private final String name;

  private final Map<String, PropertyShape> propertyShapes;

  public PropertyShape getPropertyShape(String name) {
    PropertyShape propertyShape = this.propertyShapes.get(name);

    // We need to validate the graphql definition against the shacl definition on bootstrap
    if (propertyShape == null) {
      throw new InvalidConfigurationException("No property shape found for name '{}'", name);
    }

    return propertyShape;
  }
}
