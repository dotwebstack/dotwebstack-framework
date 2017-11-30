package org.dotwebstack.framework.param;

import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.model.IRI;

public interface Parameter<T> {

  IRI getIdentifier();

  String getName();

  boolean isRequired();

  /**
   * Validates the supplied values.
   * 
   * @throws BackendException If a supplied value is invalid.
   */
  void validate(@NonNull Map<String, String> parameterValues);

  T handle(@NonNull Map<String, String> parameterValues);

}
