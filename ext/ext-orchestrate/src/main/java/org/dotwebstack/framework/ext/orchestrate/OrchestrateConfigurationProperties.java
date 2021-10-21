package org.dotwebstack.framework.ext.orchestrate;

import java.net.URI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dotwebstack.orchestrate")
class OrchestrateConfigurationProperties {

  private URI endpoint;

  private String bearerAuth;
}
