package org.dotwebstack.framework.param.types;

import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.AbstractParameter;
import org.dotwebstack.framework.param.BindableParameter;
import org.eclipse.rdf4j.model.IRI;

public abstract class TermParameter<T> extends AbstractParameter<T>
    implements BindableParameter<T> {

  // XXX (PvH) Waarom is de defaultValue een String? (en niet van het type T)?
  protected final String defaultValue;

  protected TermParameter(@NonNull IRI identifier, @NonNull String name, boolean required,
      String defaultValue) {
    super(identifier, name, required);
    this.defaultValue = defaultValue;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  // XXX (PvH) Als defaultValue van het type T is, dan kan je hier direct de defaultValue teruggeven
  // (ipv hem te wrappen in de handleInner).
  // Let op dat hiermee de changes op de *TermParameter#handleInner methods kunnen vervallen
  @Override
  protected T handleInner(Map<String, String> parameterValues) {
    String value = parameterValues.get(getName());
    return value != null ? handleInner(value) : handleInner(defaultValue);
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
