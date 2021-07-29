package org.dotwebstack.framework.service.graphql;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.zalando.problem.ProblemModule;

@Configuration
public class JacksonConfiguration {

  @Bean
  public Module javaTimeModule() {
    return new JavaTimeModule();
  }

  @Bean
  public Module problemModule() {
    return new ProblemModule();
  }

  @Bean
  public Jackson2ObjectMapperBuilder objectMapperBuilder(List<Module> modules) {
    var builder = new Jackson2ObjectMapperBuilder();

    builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT)
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .modules(modules);

    return builder;
  }
}
