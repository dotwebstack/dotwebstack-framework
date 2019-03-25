package org.dotwebstack.framework.core;

import java.util.Collection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreConfiguration {

  @Bean
  BackendRegistry backendRegistry(Collection<BackendConfigurer> configurers) {
    BackendRegistry registry = new BackendRegistry();
    configurers.forEach(configurer -> configurer.registerBackends(registry));
    return registry;
  }

}
