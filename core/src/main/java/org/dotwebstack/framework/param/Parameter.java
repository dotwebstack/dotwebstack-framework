package org.dotwebstack.framework.param;

import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.model.Resource;

public interface Parameter<T> {

  Resource getIdentifier();

  String getName();

  boolean isRequired();

  /**
   * Handles and validates the supplied values.
   *
   * @throws BackendException If a supplied value is invalid.
   */
  T handle(@NonNull Map<String, String> parameterValues);

}
