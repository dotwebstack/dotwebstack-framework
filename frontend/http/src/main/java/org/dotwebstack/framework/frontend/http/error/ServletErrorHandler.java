package org.dotwebstack.framework.frontend.http.error;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import org.glassfish.jersey.process.Inflector;

public class ServletErrorHandler implements Inflector<ContainerRequestContext, Response> {

  @Override
  public Response apply(@NonNull ContainerRequestContext context) {
    String statusCode = context.getUriInfo().getPathParameters().getFirst(
        ErrorModule.SERVLET_ERROR_STATUS_CODE_PARAMETER);

    if (statusCode == null) {
      throw new IllegalArgumentException(String.format("Path parameter '%s' is required.",
          ErrorModule.SERVLET_ERROR_STATUS_CODE_PARAMETER));
    }

    Status responseStatus = Status.fromStatusCode(Integer.parseInt(statusCode));

    if (responseStatus == null) {
      throw new IllegalArgumentException(String.format("Status code '%s' is unknown.", statusCode));
    }

    return Response.status(responseStatus).type(MediaType.TEXT_PLAIN_TYPE).entity(
        responseStatus.getReasonPhrase()).build();
  }

}
