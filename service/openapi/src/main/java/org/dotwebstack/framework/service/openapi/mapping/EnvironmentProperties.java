package org.dotwebstack.framework.service.openapi.mapping;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentProperties {

  private final Map<String, String> envProperties;

  public EnvironmentProperties(@NonNull Environment environment) {
    MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
    this.envProperties = StreamSupport.stream(propertySources.spliterator(), false)
        .filter(ps -> ps instanceof EnumerablePropertySource)
        .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
        .flatMap(Arrays::stream)
        .filter(propertyName -> {
          try {
            environment.getProperty(propertyName);
            return true;
          } catch (IllegalArgumentException e) {
            return false;
          }
        })
        .collect(Collectors.toMap(propertyName -> propertyName, environment::getProperty));
  }

  public Map<String, String> getAllProperties() {
    return this.envProperties;
  }
}
