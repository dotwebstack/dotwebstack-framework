package org.dotwebstack.framework.frontend.openapi;

import io.swagger.parser.SwaggerParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  public SwaggerParser openApiParser() {
    return new SwaggerParser();
  }

}
