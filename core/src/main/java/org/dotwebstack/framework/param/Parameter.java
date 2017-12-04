package org.dotwebstack.framework.param;

import java.util.Map;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public interface Parameter<T> {

  IRI getIdentifier();

  String getName();

  boolean isRequired();

  /**
   * Handles and validates the supplied values.
   * 
   * @throws BackendException If a supplied value is invalid.
   */
  T handle(@NonNull Map<String, String> parameterValues);

}
