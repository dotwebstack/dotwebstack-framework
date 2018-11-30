package org.dotwebstack.framework.frontend.openapi;

import io.swagger.v3.parser.OpenAPIV3Parser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  public OpenAPIV3Parser openApiParser() {
    return new OpenAPIV3Parser();
  }

}