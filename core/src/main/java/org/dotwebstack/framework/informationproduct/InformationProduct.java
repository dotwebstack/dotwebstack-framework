package org.dotwebstack.framework.informationproduct;

import org.dotwebstack.framework.backend.ResultType;
import org.eclipse.rdf4j.model.IRI;

public interface InformationProduct {
  IRI getIdentifier();

  String getLabel();

  Object getResult();

  ResultType getResultType();
}
