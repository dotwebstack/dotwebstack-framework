package org.dotwebstack.framework.param;

import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;

public final class TermParameterDefinition extends ParameterDefinition {

  public TermParameterDefinition(IRI identifier, String name, Optional<PropertyShape> shapeType) {
    super(identifier, name, shapeType);
  }
}
