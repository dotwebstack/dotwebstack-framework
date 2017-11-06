package org.dotwebstack.framework.frontend.openapi.handlers;

import org.dotwebstack.framework.param.AbstractParameter;
import org.eclipse.rdf4j.model.IRI;

public final class TestParameter extends AbstractParameter {

  public TestParameter(IRI identifier, String name) {
    super(identifier, name);
  }

  @Override
  public String handle(String value, String query) {
    throw new UnsupportedOperationException(
        "handle() method unsupported, mock the Parameter interface instead");
  }

}
