package org.dotwebstack.framework.product.config;

import java.util.HashMap;
import org.dotwebstack.framework.product.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductRegistry {

  HashMap<String, Product> products = new HashMap<>();

  public void registerProduct(String productName, Product product) {
    products.put(productName, product);
  }

  public Product getProduct(String productName) {
    if (!products.containsKey(productName)) {
      throw new IllegalArgumentException(
          String.format("Product with name \"%s\" not found.", productName));
    }

    return products.get(productName);
  }

}
