package org.dotwebstack.framework.frontend.http;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonModulesConfiguration {

  @Bean
  public Module guavaModule() {
    return new GuavaModule();
  }

}
