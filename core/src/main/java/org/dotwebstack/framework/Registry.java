package org.dotwebstack.framework;

import java.util.HashMap;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Service;

@Service
public class Registry {

  private HashMap<IRI, InformationProduct> informationProducts = new HashMap<>();

  public void registerInformationProduct(InformationProduct product) {
    informationProducts.put(product.getIdentifier(), product);
  }

  public InformationProduct getInformationProduct(IRI identifier) {
    if (!informationProducts.containsKey(identifier)) {
      throw new IllegalArgumentException(
          String.format("Product with name \"%s\" not found.", identifier.toString()));
    }

    return informationProducts.get(identifier);
  }

  public int getNumberOfInformationProducts() {
    return informationProducts.size();
  }

}
