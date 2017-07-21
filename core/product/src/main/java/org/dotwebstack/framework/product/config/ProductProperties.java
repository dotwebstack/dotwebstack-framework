package org.dotwebstack.framework.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dotwebstack.product")
public class ProductProperties {

}
