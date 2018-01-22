package org.dotwebstack.framework.frontend.http;

import javax.ws.rs.HttpMethod;
import org.glassfish.jersey.server.model.Resource;
import org.springframework.stereotype.Service;

@Service
public class ErrorIntegrationModule implements HttpModule {

  @Override
  public void initialize(HttpConfiguration httpConfiguration) {
    Resource.Builder builder = Resource.builder("/{domain}/runtime-exception");
    builder.addMethod(HttpMethod.GET).handledBy(containerRequestContext -> {
      throw new RuntimeException("Message containing sensitive debug info");
    });
    httpConfiguration.registerResources(builder.build());
  }

}
