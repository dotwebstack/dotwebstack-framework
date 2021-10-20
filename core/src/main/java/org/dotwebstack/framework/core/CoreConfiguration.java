package org.dotwebstack.framework.core;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfigurationReader;
import org.dotwebstack.framework.core.config.validators.DotWebStackConfigurationValidator;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
@Configuration
public class CoreConfiguration {

  @Bean
  @Conditional(ConfigurationExistsCondition.class)
  public DotWebStackConfiguration dotWebStackConfiguration(
      @Value("${dotwebstack.config:dotwebstack.yaml}") String configFilename,
      List<DotWebStackConfigurationValidator> validators) {
    LOG.info("Using DotWebStackConfiguration: {}", configFilename);
    var dotWebStackConfigurationReader = new DotWebStackConfigurationReader();
    var dotWebStackConfiguration = dotWebStackConfigurationReader.read(configFilename);

    if (dotWebStackConfiguration.getObjectTypes() != null) {
      dotWebStackConfiguration.getObjectTypes()
          .values()
          .forEach(objectType -> objectType.init(dotWebStackConfiguration));
    }

    validators.forEach(validator -> validator.validate(dotWebStackConfiguration));

    return dotWebStackConfiguration;
  }

  public static class ConfigurationExistsCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
      var resourceName = context.getEnvironment()
          .getProperty("dotwebstack.config", "dotwebstack.yaml");

      return ResourceLoaderUtils.getResource(resourceName)
          .isPresent();
    }
  }
}
