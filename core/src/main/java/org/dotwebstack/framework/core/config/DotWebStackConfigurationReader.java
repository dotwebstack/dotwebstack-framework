package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

public class DotWebStackConfigurationReader {
  private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

  public DotWebStackConfigurationReader() {
    registerSubTypes();
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  }

  public DotWebStackConfigurationReader(Class<?>... configurationClasses) {
    objectMapper.registerSubtypes(configurationClasses);
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

  public DotWebStackConfiguration readNoValidation(String filename) {
    return ResourceLoaderUtils.getResource(filename)
        .map(mapResource())
        .orElseThrow(() -> invalidConfigurationException("Config file not found on location: {}", filename));
  }

  protected Function<Resource, DotWebStackConfiguration> mapResource() {
    return resource -> {
      try {
        return objectMapper.readValue(resource.getInputStream(), DotWebStackConfiguration.class);
      } catch (IOException e) {
        throw new InvalidConfigurationException("Error while reading config file.", e);
      }
    };
  }

  public DotWebStackConfiguration read(String filename) {
    return ResourceLoaderUtils.getResource(filename)
        .map(mapResource())
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
