package org.dotwebstack.framework.service.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class OpenApiWebFluxConfigurer implements WebFluxConfigurer {

  private final ObjectMapper objectMapper;

  public OpenApiWebFluxConfigurer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
    var defaultCodes = configurer.defaultCodecs();
    defaultCodes.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
    defaultCodes.jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
  }
}
