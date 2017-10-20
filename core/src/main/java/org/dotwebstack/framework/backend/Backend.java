package org.dotwebstack.framework.backend;

import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface Backend {

  IRI getIdentifier();

  InformationProduct createInformationProduct(IRI identifier, String label, Filter filter,
      Model statements);

}
