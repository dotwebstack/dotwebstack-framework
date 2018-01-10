package org.dotwebstack.framework.param.types;

import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class IntTermParameter extends TermParameter<Integer> {

  public IntTermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  @Override
  public Literal getValue(Integer value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

  @Override
  protected Integer parseValue(Map<String, String> parameterValues) {
    return Integer.parseInt(parameterValues.get(getName()));
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
