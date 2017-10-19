package org.dotwebstack.framework.informationproduct;

import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.filters.Filter;
import org.eclipse.rdf4j.model.IRI;

public interface InformationProduct {
  IRI getIdentifier();

  String getLabel();

  Object getResult(String value);

  ResultType getResultType();

  Filter getFilter();
}
