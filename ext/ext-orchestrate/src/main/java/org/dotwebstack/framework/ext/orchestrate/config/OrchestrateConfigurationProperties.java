package org.dotwebstack.framework.ext.orchestrate.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.Map;
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

    private Map<String, String> headers;

    @NotNull
    private URI endpoint;

    private String bearerAuth;
  }
}
