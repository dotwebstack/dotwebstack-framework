package org.dotwebstack.framework.ext.orchestrate.config;

import java.net.URI;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@RootSubschemaExists
@ConfigurationProperties(prefix = "dotwebstack.orchestrate")
class OrchestrateConfigurationProperties {

  @NotBlank
  private String root;

  @Valid
  @Size(min = 1)
  private Map<String, SubschemaProperties> subschemas;

  @Data
  static class SubschemaProperties {

    @NotNull
    private URI endpoint;

    private String bearerAuth;
  }
}
