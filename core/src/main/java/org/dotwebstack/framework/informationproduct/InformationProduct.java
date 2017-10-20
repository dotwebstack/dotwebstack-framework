package org.dotwebstack.framework.informationproduct;

import java.util.Collection;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filter.Filter;
import org.eclipse.rdf4j.model.IRI;

public interface InformationProduct {
  IRI getIdentifier();

  String getLabel();

  Object getResult(String value);

  ResultType getResultType();

  Collection<Filter> getFilters();
}
