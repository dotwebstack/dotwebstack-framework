package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.Map;
import org.dotwebstack.framework.param.AbstractParameter;
import org.eclipse.rdf4j.model.IRI;

public final class TestParameter extends AbstractParameter<Object> {

  private TestParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  public static TestParameter requiredTermParameter(IRI identifier, String name) {
    return new TestParameter(identifier, name, true);
  }

  public static TestParameter optionalTermParameter(IRI identifier, String name) {
    return new TestParameter(identifier, name, false);
  }

  @Override
  public Object handle(Map<String, Object> parameterValues) {
    throw new UnsupportedOperationException(
        "handle() method unsupported, mock the Parameter interface instead");
  }

}
