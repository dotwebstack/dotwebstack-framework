package org.dotwebstack.framework.core;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

@Slf4j
@Configuration
public class CoreConfiguration {

  private static final String CONFIG_FILE = "dotwebstack.yaml";

  @Bean
  public DotWebStackConfiguration dotWebStackConfiguration() {
    ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(TypeConfiguration.class));
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    scanner.findCandidateComponents("org.dotwebstack.framework.backend")
        .stream()
        .map(beanDefinition -> ClassUtils.resolveClassName(Objects.requireNonNull(beanDefinition.getBeanClassName()),
            ClassLoader.getSystemClassLoader()))
        .forEach(objectMapper::registerSubtypes);

    return ResourceLoaderUtils.getResource(CONFIG_FILE)
        .map(resource -> {
          try {
            return objectMapper.readValue(resource.getFile(), DotWebStackConfiguration.class);
          } catch (IOException e) {
            throw new InvalidConfigurationException("Error while reading config file.", e);
          }
        })
        .map(dotWebStackConfiguration -> {
          Set<ConstraintViolation<DotWebStackConfiguration>> violations = Validation.buildDefaultValidatorFactory()
              .getValidator()
              .validate(dotWebStackConfiguration);

          if (!violations.isEmpty()) {
            throw invalidConfigurationException("Config file contains validation errors: {}", violations);
          }

          return dotWebStackConfiguration;
        })
        .orElseThrow(() -> invalidConfigurationException("Config file not found on location: {}", CONFIG_FILE));
  }
}
