package org.dotwebstack.framework.param;

import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;

public final class TermParameterDefinition extends ParameterDefinition {

  private final Optional<PropertyShape> shapeType;

  public TermParameterDefinition(IRI identifier, String name, Optional<PropertyShape> shapeType) {
    super(identifier, name);
    this.shapeType = shapeType;
  }

  public Optional<PropertyShape> getShapeType() {
    return shapeType;
  }
}
