package org.dotwebstack.framework.frontend.http;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.HttpMethod;
import org.dotwebstack.framework.frontend.http.error.InvalidParamsBadRequestException;
import org.dotwebstack.framework.frontend.http.error.InvalidParamsBadRequestException.InvalidParameter;
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
          Map<String, String> details = new HashMap<>();
          details.put("detailkey", "detailvalue");
          details.put("otherkey", "othervalue");
          ImmutableList<InvalidParameter> invalidParameters =
              ImmutableList.of(new InvalidParameter("detailkey", "detailvalue"), //
                  new InvalidParameter("otherkey", "othervalue"));
          throw new InvalidParamsBadRequestException("extended-message", invalidParameters);
        });
    httpConfiguration.registerResources(otherBuilder.build());
  }

}
