package org.dotwebstack.framework.frontend.http.error;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.HttpModule;
import org.glassfish.jersey.server.model.Resource;
import org.springframework.stereotype.Service;

@Service
public class ErrorModule implements HttpModule {

  static final String SERVLET_ERROR_PATH_PREFIX = "__errors";

  static final String SERVLET_ERROR_STATUS_CODE_PARAMETER = "statusCode";

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    Resource.Builder resourceBuilder =
        Resource.builder().path(String.format("/{domain}/%s/{%s:\\d{3}}",
            SERVLET_ERROR_PATH_PREFIX, SERVLET_ERROR_STATUS_CODE_PARAMETER));
    resourceBuilder.addMethod(HttpMethod.GET).handledBy(new ServletErrorHandler()).produces(
        MediaType.TEXT_PLAIN_TYPE);
    httpConfiguration.registerResources(resourceBuilder.build());
  }

}
