package org.dotwebstack.framework.param;

import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.model.Resource;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public abstract class AbstractParameter<T> implements Parameter<T> {

  @NonNull
  @Getter
  Resource identifier;

  @NonNull
  @Getter
  String name;

  boolean required;

  @Override
  public boolean isRequired() {
    return required;
  }

  /**
   * Validates and handles the supplied values. Calls {@link #validateRequired(Map)} and
   * {@link #validateInner(Map)} for validation. Calls {@link #handleInner(Map)} for handling.
   *
   * @throws BackendException If a supplied value is invalid.
   */
  @Override
  public final T handle(@NonNull Map<String, String> parameterValues) {
    validate(parameterValues);

    return handleInner(parameterValues);
  }

  /**
   * Must be implemented by parameter implementations for parameter handling.
   */
  protected abstract T handleInner(@NonNull Map<String, String> parameterValues);

  private void validate(Map<String, String> parameterValues) {
    validateInner(parameterValues);

    if (required) {
      validateRequired(parameterValues);
    }
  }

  /**
   * Must be implemented by parameter implementations to validate the required case. See
   * implementations of this class for examples.
   *
   * @throws BackendException If a required value is missing.
   */
  protected abstract void validateRequired(@NonNull Map<String, String> parameterValues);

  /**
   * Implement this method if you would like to do parameter implementation specific validation. See
   * implementations of this class for examples. By default, does nothing.
   *
   * @throws BackendException If a required value is invalid.
   */
  protected void validateInner(@NonNull Map<String, String> parameterValues) {}

}
