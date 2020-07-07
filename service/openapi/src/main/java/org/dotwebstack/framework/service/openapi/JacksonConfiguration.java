package org.dotwebstack.framework.service.openapi;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfiguration {

  private OpenApiProperties openApiProperties;

  public JacksonConfiguration(OpenApiProperties openApiProperties) {
    this.openApiProperties = openApiProperties;
  }

  @Bean
  public Module javaTimeModule() {
    return new JavaTimeModule();
  }

  @Bean
  public Jackson2ObjectMapperBuilder objectMapperBuilder(List<Module> modules) {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT)
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .modules(modules);
    if (!openApiProperties.isSerializeNull()) {
      builder.serializationInclusion(Include.NON_NULL);
    }
    return builder;
  }
}
