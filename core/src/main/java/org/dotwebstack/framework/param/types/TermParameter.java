package org.dotwebstack.framework.param.types;

import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.AbstractParameter;
import org.dotwebstack.framework.param.BindableParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class TermParameter extends AbstractParameter<String> implements BindableParameter<String> {

  public TermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  public static TermParameter requiredTermParameter(IRI identifier, String name) {
    return new TermParameter(identifier, name, true);
  }

  public static TermParameter optionalTermParameter(IRI identifier, String name) {
    return new TermParameter(identifier, name, false);
  }

  @Override
  protected String handleInner(Map<String, String> parameterValues) {
    return parseValue(parameterValues);
  }

  @Override
  public Literal getValue(String value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

  @Override
  public String parseValue(Map<String, String> parameterValues) {
    return parameterValues.get(getName());
  }

  @Override
  protected void validateRequired(Map<String, String> parameterValues) {
    if (parseValue(parameterValues) == null) {
      throw new BackendException(
          String.format("No value found for required parameter '%s'. Supplied parameterValues: %s",
              getIdentifier(), parameterValues));
    }
  }



}
