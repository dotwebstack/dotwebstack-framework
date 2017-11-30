package org.dotwebstack.framework.param;

import java.util.Map;
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

  /**
   * Validates the supplied values. Calls {@link #validateRequired(Map)} and
   * {@link #validateInner(Map)}.
   * 
   * @throws BackendException If a supplied value is invalid.
   */
  @Override
  public void validate(Map<String, String> parameterValues) {
    if (required) {
      validateRequired(parameterValues);
    }

    validateInner(parameterValues);
  }

  // @Override
  // public final T handle(Map<String, Object> parameterValues) {
  // validate(parameterValues);
  //
  // return handleInner();
  // }
  //
  // protected abstract T handleInner();

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
  protected void validateInner(@NonNull Map<String, String> parameterValues) {}

}
