package org.dotwebstack.framework.ext.proxy;

import java.net.URI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "dotwebstack.proxy")
class ProxyConfigurationProperties {

  private URI endpoint;

  private String bearerAuth;
}
