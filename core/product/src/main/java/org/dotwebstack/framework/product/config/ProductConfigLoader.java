package org.dotwebstack.framework.product.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductConfigLoader {

  @SuppressWarnings("unused")
  private ProductProperties productProperties;

  @SuppressWarnings("unused")
  private ProductRegistry productRegistry;

  @Autowired
  public ProductConfigLoader(ProductProperties productProperties, ProductRegistry productRegistry) {
    this.productProperties = productProperties;
    this.productRegistry = productRegistry;
  }

  @PostConstruct
  public void load() {
    throw new UnsupportedOperationException();
  }

}
