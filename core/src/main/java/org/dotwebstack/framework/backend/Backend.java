package org.dotwebstack.framework.backend;

import java.util.Collection;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface Backend {

  IRI getIdentifier();

  InformationProduct createInformationProduct(IRI identifier, String label,
      Collection<Parameter> parameters, Model statements);

}
