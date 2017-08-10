package org.dotwebstack.framework;

import java.util.Optional;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.springframework.stereotype.Service;

@Service
class InformationProductFactory {

  public InformationProduct create(Model model, IRI identifier) {
    InformationProduct informationProduct = new InformationProduct(identifier);
    Optional<String> label = Models.objectString(model.filter(identifier, RDFS.LABEL, null));

    if (label.isPresent()) {
      informationProduct.setLabel(label.get());
    }

    return informationProduct;
  }

}
