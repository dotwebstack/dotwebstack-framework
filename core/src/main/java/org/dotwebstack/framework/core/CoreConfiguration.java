package org.dotwebstack.framework.core;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfigurationReader;
import org.dotwebstack.framework.core.config.validators.DotWebStackConfigurationValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CoreConfiguration {

  private static final String CONFIG_FILE = "dotwebstack.yaml";

  @Bean
  public DotWebStackConfiguration dotWebStackConfiguration(List<DotWebStackConfigurationValidator> validators) {
    var dotWebStackConfigurationReader = new DotWebStackConfigurationReader();
    var dotWebStackConfiguration = dotWebStackConfigurationReader.read(CONFIG_FILE);

    dotWebStackConfiguration.getObjectTypes()
        .values()
        .forEach(objectType -> objectType.init(dotWebStackConfiguration));

    validators.forEach(validator -> validator.validate(dotWebStackConfiguration));

    return dotWebStackConfiguration;
  }
}
