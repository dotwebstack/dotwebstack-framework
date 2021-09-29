package org.dotwebstack.framework.core;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfigurationReader;
import org.dotwebstack.framework.core.config.validators.DotWebStackConfigurationValidator;
import org.dotwebstack.framework.core.graphql.GraphQlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CoreConfiguration {

  public static DotWebStackConfiguration dotWebStackConfiguration;

  @Bean
  public GraphQlService graphQlService(List<GraphQlService> beans) {
    return beans.get(0);
  }

  @Bean
  public DotWebStackConfiguration dotWebStackConfiguration(
      @Value("${dotwebstack.config:dotwebstack.yaml}") String configFilename,
      List<DotWebStackConfigurationValidator> validators) {
    var dotWebStackConfigurationReader = new DotWebStackConfigurationReader();
    var dotWebStackConfiguration = dotWebStackConfigurationReader.read(configFilename);

    validateDotWebStackConfiguration(validators, dotWebStackConfiguration);
    initObjectTypes(dotWebStackConfiguration);

    return dotWebStackConfiguration;
  }

  private void initObjectTypes(DotWebStackConfiguration dotWebStackConfiguration) {
    if (dotWebStackConfiguration.getObjectTypes() != null) {
      dotWebStackConfiguration.getObjectTypes()
          .values()
          .forEach(objectType -> objectType.init(dotWebStackConfiguration));
    }
  }

  private void validateDotWebStackConfiguration(List<DotWebStackConfigurationValidator> validators,
      DotWebStackConfiguration dotWebStackConfiguration) {
    validators.forEach(validator -> validator.validate(dotWebStackConfiguration));
  }
}
