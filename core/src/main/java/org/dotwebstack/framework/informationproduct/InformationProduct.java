package org.dotwebstack.framework.informationproduct;

import java.util.Collection;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.Resource;

public interface InformationProduct {

  Resource getIdentifier();

  String getLabel();

  /**
   * @param parameterValues Mapping of parameter name to value.
   * @throws BackendException If no value of for required parameter has been supplied.
   */
  Object getResult(Map<String, String> parameterValues);

  ResultType getResultType();

  Collection<Parameter> getParameters();

}
