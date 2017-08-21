package org.dotwebstack.framework;

import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public ConfigurationBackend configurationBackend() {
    return new FileConfigurationBackend();
  }

}
