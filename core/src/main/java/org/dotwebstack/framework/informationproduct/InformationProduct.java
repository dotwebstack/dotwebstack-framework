package org.dotwebstack.framework.informationproduct;

import java.util.Collection;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;

public interface InformationProduct {

  IRI getIdentifier();

  String getLabel();

  /**
   * @param values Mapping of parameter name to value.
   * @throws BackendException If no value of for required parameter has been supplied.
   */
  // TODO Mapping of Parameter instance to value? (instead of parameter String name to value)
  Object getResult(Map<String, String> values);

  ResultType getResultType();

  Collection<Parameter> getParameters();

}
