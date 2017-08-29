package org.dotwebstack.framework.frontend.http;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.stereotype.Service;

@Service
public class HttpConfiguration extends ResourceConfig {

  public HttpConfiguration() {
    super();
    packages("org.dotwebstack.framework.frontend.http");
    property(ServerProperties.WADL_FEATURE_DISABLE, true);
  }

}
