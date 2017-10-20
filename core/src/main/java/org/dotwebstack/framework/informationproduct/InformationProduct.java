package org.dotwebstack.framework.informationproduct;

import java.util.Collection;
import java.util.Map;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filter.Filter;
import org.eclipse.rdf4j.model.IRI;

public interface InformationProduct {
  IRI getIdentifier();

  String getLabel();

  /**
   * @param values Mapping of filter name to value.
   */
  // TODO Mapping of Filter to value? (instead of filter String name to value)
  Object getResult(Map<String, String> values);

  ResultType getResultType();

  Collection<Filter> getFilters();
}
