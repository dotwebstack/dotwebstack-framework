package org.dotwebstack.framework.frontend.http;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
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
  public Jackson2ObjectMapperBuilder objectMapperBuilder(List<Module> modules) {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT).modules(modules);
    return builder;
  }

}
