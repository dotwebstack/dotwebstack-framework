package org.dotwebstack.framework.param.types;

import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.AbstractParameter;
import org.dotwebstack.framework.param.BindableParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class StringTermParameter extends AbstractParameter<String>
    implements BindableParameter<String> {

  public StringTermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  @Override
  protected String handleInner(Map<String, String> parameterValues) {
    return getValue(parameterValues);
  }

  @Override
  public Literal getValue(String value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

  private String getValue(Map<String, String> parameterValues) {
    return parameterValues.get(getName());
  }

  @Override
  protected void validateRequired(Map<String, String> parameterValues) {
    if (getValue(parameterValues) == null) {
      throw new BackendException(
          String.format("No value found for required parameter '%s'. Supplied parameterValues: %s",
              getIdentifier(), parameterValues));
    }
  }

}
