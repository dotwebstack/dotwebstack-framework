package org.dotwebstack.framework;

import java.util.HashMap;
import org.eclipse.rdf4j.model.IRI;
import org.springframework.stereotype.Component;

@Component
public class ProductRegistry {

  HashMap<IRI, Product> products = new HashMap<>();

  public void registerProduct(Product product) {
    products.put(product.getIdentifier(), product);
  }

  public Product getProduct(IRI identifier) {
    if (!products.containsKey(identifier)) {
      throw new IllegalArgumentException(
          String.format("Product with name \"%s\" not found.", identifier.toString()));
    }

    return products.get(identifier);
  }

  public int getNumberOfProducts() {
    return products.size();
  }

}
