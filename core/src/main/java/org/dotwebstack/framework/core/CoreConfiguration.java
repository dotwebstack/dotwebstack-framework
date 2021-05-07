package org.dotwebstack.framework.core;

import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfigurationReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CoreConfiguration {

  private static final String CONFIG_FILE = "dotwebstack.yaml";

  @Bean
  public DotWebStackConfiguration dotWebStackConfiguration() {
    // TODO: refactor matching? Annotation-based?
    DotWebStackConfigurationReader dotWebStackConfigurationReader = new DotWebStackConfigurationReader();
    return dotWebStackConfigurationReader.read(CONFIG_FILE);
  }
}
