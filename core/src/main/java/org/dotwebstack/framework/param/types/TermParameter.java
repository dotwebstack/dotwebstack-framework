package org.dotwebstack.framework.param.types;

import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.AbstractParameter;
import org.dotwebstack.framework.param.BindableParameter;
import org.eclipse.rdf4j.model.IRI;

public abstract class TermParameter<T> extends AbstractParameter<T>
    implements BindableParameter<T> {

  private T defaultValue = null;

  protected TermParameter(IRI identifier, String name, boolean required) {
    super(identifier, name, required);
  }

  protected TermParameter(IRI identifier, String name, boolean required, T defaultValue) {
    super(identifier, name, required);
    this.defaultValue = defaultValue;
  }

  @Override
  protected T handleInner(Map<String, String> parameterValues) {
    String value = parameterValues.get(getName());
    return value != null ? handleInner(value) : defaultValue;
  }

  protected abstract T handleInner(String value);

  @Override
  protected void validateRequired(Map<String, String> parameterValues) {
    if (handleInner(parameterValues) == null) {
      throw new BackendException(
          String.format("No value found for required parameter '%s'. Supplied parameterValues: %s",
              getIdentifier(), parameterValues));
    }
  }

}
