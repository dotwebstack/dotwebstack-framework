package org.dotwebstack.framework.param.types;

import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.AbstractParameter;
import org.dotwebstack.framework.param.BindableParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class IntTermParameter extends AbstractParameter<Integer>
    implements BindableParameter<Integer> {

  public IntTermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  @Override
  protected Integer handleInner(Map<String, String> parameterValues) {
    return getValue(parameterValues);
  }

  @Override
  public Literal getValue(Integer value) {
    return SimpleValueFactory.getInstance().createLiteral(value);
  }

  private Integer getValue(Map<String, String> parameterValues) {
    return Integer.parseInt(parameterValues.get(getName()));
  }

  @Override
  protected void validateRequired(Map<String, String> parameterValues) {
    try {
      getValue(parameterValues);
    } catch (NumberFormatException | NullPointerException e) {
      throw new BackendException(
          String.format("No value found for required parameter '%s'. Supplied parameterValues: %s",
              getIdentifier(), parameterValues));
    }
  }

  protected void validateInner(@NonNull Map<String, String> parameterValues) {

  }


}
