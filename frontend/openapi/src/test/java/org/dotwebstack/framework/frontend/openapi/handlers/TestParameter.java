package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.Map;
import org.dotwebstack.framework.param.AbstractParameter;
import org.eclipse.rdf4j.model.IRI;

public final class TestParameter extends AbstractParameter<Object> {

  public TestParameter(IRI identifier, String name) {
    super(identifier, name);
  }

  @Override
  public Object handle(Map<String, Object> parameterValues) {
    throw new UnsupportedOperationException(
        "handle() method unsupported, mock the Parameter interface instead");
  }

}
