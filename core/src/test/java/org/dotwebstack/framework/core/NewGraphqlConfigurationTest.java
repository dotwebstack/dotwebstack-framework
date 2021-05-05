package org.dotwebstack.framework.core;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

public class NewGraphqlConfigurationTest {

  @Test
  public void typeDefinitionRegistry_registerObjectTypes_whenConfigured() {
    var dotWebStackConfiguration = readDotWebStackConfiguration("dotwebstack/dotwebstack-all.yaml");
    assertThat(dotWebStackConfiguration, is(notNullValue()));

  }

  private DotWebStackConfiguration readDotWebStackConfiguration(String filename) {
    // var objectMapper = new ObjectMapper(new YAMLFactory());
    // objectMapper.registerSubtypes();
    var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(TypeConfiguration.class));
    var objectMapper = new ObjectMapper(new YAMLFactory());

    scanner.findCandidateComponents("org.dotwebstack.framework.core.config")
        .stream()
        .map(beanDefinition -> ClassUtils.resolveClassName(Objects.requireNonNull(beanDefinition.getBeanClassName()),
            getClass().getClassLoader()))
        .forEach(objectMapper::registerSubtypes);
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
