package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

public class DotWebStackConfigurationReader {
  private static ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

  public DotWebStackConfigurationReader() {
    registerSubTypes();
  }

  public DotWebStackConfigurationReader(Class<?>... configurationClasses) {
    objectMapper.registerSubtypes(configurationClasses);
  }

  private void registerSubTypes() {
    var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(TypeConfiguration.class));
    scanner.findCandidateComponents("org.dotwebstack.framework.backend")
        .stream()
        .map(beanDefinition -> ClassUtils.resolveClassName(Objects.requireNonNull(beanDefinition.getBeanClassName()),
            getClass().getClassLoader()))
        .forEach(objectMapper::registerSubtypes);
  }

  public DotWebStackConfiguration read(String filename) {
    return ResourceLoaderUtils.getResource(filename)
        .map(resource -> {
          try {
            return objectMapper.readValue(resource.getInputStream(), DotWebStackConfiguration.class);
          } catch (IOException e) {
            throw new InvalidConfigurationException("Error while reading config file.", e);
          }
        })
        .map(configuration -> {
          Set<ConstraintViolation<DotWebStackConfiguration>> violations = Validation.buildDefaultValidatorFactory()
              .getValidator()
              .validate(configuration);

          if (!violations.isEmpty()) {
            throw invalidConfigurationException("Config file contains validation errors: {}", violations);
          }

          return configuration;
        })
        .orElseThrow(() -> invalidConfigurationException("Config file not found on location: {}", filename));
  }
}
