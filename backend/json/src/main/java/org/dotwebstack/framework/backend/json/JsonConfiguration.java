package org.dotwebstack.framework.backend.json;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Deprecated
@Configuration
@EnableConfigurationProperties(JsonProperties.class)
public class JsonConfiguration {

  private final JsonProperties jsonProperties;

  public JsonConfiguration(JsonProperties jsonProperties) {
    this.jsonProperties = jsonProperties;
  }
}
