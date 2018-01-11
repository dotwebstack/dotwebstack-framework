package org.dotwebstack.framework.param;

import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.model.IRI;

public abstract class AbstractParameter<T> implements Parameter<T> {

  private final IRI identifier;

  private final String name;

  private final boolean required;

  protected AbstractParameter(@NonNull IRI identifier, @NonNull String name, boolean required) {
    this.identifier = identifier;
    this.name = name;
    this.required = required;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isRequired() {
    return required;
  }


  public Optional<T> parseRawValue(Map<String, String> parameterValues) {
    String value = parameterValues.get(getName());
    if (value != null) {
      return Optional.of((T) parseValue(value));
    }
    return Optional.empty();
  }

  /**
   * Validates and handles the supplied values. Calls {@link #validateRequired(Map)} and
   * {@link #validateInner(Map)} for validation. Calls {@link #handleInner(Map)} for handling.
   * 
   * @throws BackendException If a supplied value is invalid.
   */
  @Override
  public final T handle(Map<String, String> parameterValues) {
    validate(parameterValues);

    return handleInner(parameterValues);
  }

  /**
   * Must be implemented by parameter implementations for parameter handling.
   */
  protected T handleInner(Map<String, String> parameterValues) {
    return parseValue(parameterValues);
  }

  private void validate(Map<String, String> parameterValues) {
    validateInner(parameterValues);
    if (required) {
      validateRequired(parameterValues);
    }
  }


  public T parseValue(Map<String, String> parameterValues) {
    return parseRawValue(parameterValues).orElse(null);
  }

  protected abstract T parseValue(String value);

  /**
   * Must be implemented by parameter implementations to validate the required case. See
   * implementations of this class for examples.
   * 
   * @throws BackendException If a required value is missing.
   */
  protected abstract void validateRequired(Map<String, String> parameterValues);

  /**
   * Implement this method if you would like to do parameter implementation specific validation. See
   * implementations of this class for examples. By default, does nothing.
   * 
   * @throws BackendException If a required value is invalid.
   */
  protected void validateInner(@NonNull Map<String, String> parameterValues) {
    try {
      parseValue(parameterValues);
    } catch (NumberFormatException e) {
      throw new BackendException("Value could not be parsed.");
    }
  }

}
