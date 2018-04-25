package org.dotwebstack.framework.frontend.http;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfiguration {

  @Bean
  public Module guavaModule() {
    return new GuavaModule();
  }

  @Bean
  public Module javaTimeModule() {
    return new JavaTimeModule();
  }

  @Bean
  public Jackson2ObjectMapperBuilder objectMapperBuilder(List<Module> modules) {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT) //
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) //
        .modules(modules);
    return builder;
  }

}
