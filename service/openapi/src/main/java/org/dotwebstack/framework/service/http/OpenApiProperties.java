package org.dotwebstack.framework.service.http;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "dotwebstack.openapi")
public class OpenApiProperties {

  @NotNull
  private String specificationFile = "config/model/openapi.yml";
}
