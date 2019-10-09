package org.dotwebstack.framework.service.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.dotwebstack.framework.core.CoreProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiSchemaConfiguration {

  private static final String SPEC_FILENAME = "openapi.yaml";

  private final CoreProperties coreProperties;

  public OpenApiSchemaConfiguration(CoreProperties properties) {
    this.coreProperties = properties;
  }

  @Bean
  public OpenAPI openApi() {
    return new OpenAPIV3Parser().read(coreProperties.getResourcePath()
        .resolve(SPEC_FILENAME)
        .getPath());
  }
}
