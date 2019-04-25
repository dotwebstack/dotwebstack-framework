package org.dotwebstack.framework.core;

import java.net.URI;
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
  private URI resourcePath = URI.create(CLASSPATH_PREFIX + "config/");

}
