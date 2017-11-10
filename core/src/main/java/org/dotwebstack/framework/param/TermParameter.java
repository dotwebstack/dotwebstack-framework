package org.dotwebstack.framework.param;

import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.model.IRI;

public class TermParameter extends AbstractParameter<String> {

  private TermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  public static TermParameter requiredTermParameter(IRI identifier, String name) {
    return new TermParameter(identifier, name, true);
  }

  public static TermParameter optionalTermParameter(IRI identifier, String name) {
    return new TermParameter(identifier, name, false);
  }

  @Override
  public String handle(@NonNull Map<String, Object> parameterValues) {
    return (String) getValue(parameterValues);
  }

  @Override
  protected void validateRequired(Map<String, Object> parameterValues) {
    if (getValue(parameterValues) == null) {
      throw new BackendException(
          String.format("No value found for required parameter '%s'. Supplied parameterValues: %s",
              getIdentifier(), parameterValues));
    }
  }

  /**
   * @throws BackendException If the value is not a String.
   */
  @Override
  protected void validateInner(Map<String, Object> parameterValues) {
    Object value = getValue(parameterValues);

    if (value != null && !(value instanceof String)) {
      throw new BackendException(
          String.format("Value for parameter '%s' not a String: '%s'", getIdentifier(), value));
    }
  }

  private Object getValue(Map<String, Object> parameterValues) {
    return parameterValues.get(getName());
  }

}
