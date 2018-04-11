package org.dotwebstack.framework.frontend.http;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.frontend.http.error.ExtendedProblemDetailException;
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
    Resource.Builder otherBuilder = Resource.builder("/{domain}/extended-exception");
    otherBuilder.addMethod(HttpMethod.GET) //
        .handledBy(context -> {
          Map<String, Object> innerDetails = new HashMap<>();
          innerDetails.put("name", "dummy");
          innerDetails.put("reason", "because I said so");
          Map<String, Object> details = new HashMap<>();
          details.put("detailkey", innerDetails);
          throw new ExtendedProblemDetailException("extended-message", Status.BAD_REQUEST, details);
        });
    httpConfiguration.registerResources(otherBuilder.build());
  }

}
