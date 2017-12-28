package org.dotwebstack.framework.param.types;

import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.AbstractParameter;
import org.dotwebstack.framework.param.BindableParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class BooleanTermParameter extends AbstractParameter<Boolean>
    implements BindableParameter<Boolean> {

  public BooleanTermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  @Override
  protected Boolean handleInner(Map<String, String> parameterValues) {
    return parseValue(parameterValues);
  }

  @Override
  protected Boolean parseValue(Map<String, String> parameterValues) {
    return Boolean.valueOf(parameterValues.get(getName()));
  }

  @Override
  public Literal getValue(Boolean value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

  @Override
  protected void validateRequired(Map<String, String> parameterValues) {
    try {
      parseValue(parameterValues);
    } catch (NumberFormatException | NullPointerException e) {
      throw new BackendException(
          String.format("No value found for required parameter '%s'. Supplied parameterValues: %s",
              getIdentifier(), parameterValues));
    }
  }



}
