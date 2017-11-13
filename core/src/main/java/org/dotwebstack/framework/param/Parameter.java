package org.dotwebstack.framework.param;

import java.util.Map;
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
  void validate(Map<String, Object> parameterValues);

  T handle(Map<String, Object> parameterValues);

}
