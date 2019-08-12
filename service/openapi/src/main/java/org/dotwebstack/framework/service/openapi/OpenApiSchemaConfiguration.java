package org.dotwebstack.framework.service.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenApiProperties.class)
public class OpenApiSchemaConfiguration {

  private final OpenApiProperties properties;

  public OpenApiSchemaConfiguration(OpenApiProperties properties) {
    this.properties = properties;
  }

  @Bean
  public OpenAPI openApi() {
    return new OpenAPIV3Parser().read(properties.getSpecificationFile());
  }
}
