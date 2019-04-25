package org.dotwebstack.framework.core;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "dotwebstack")
public class CoreProperties {

  private static final String CLASSPATH_PREFIX = "classpath:/";

  @NonNull
  private URI resourcePath;

  public CoreProperties() {
    try {
      resourcePath = new URI(CLASSPATH_PREFIX + "config/");
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("The resourcePath that you are trying to set is invalid", e);
    }
  }
}
