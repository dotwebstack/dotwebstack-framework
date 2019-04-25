package org.dotwebstack.framework.core;

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

  @NonNull
  private String resourcePath = "classpath:/config";

}
