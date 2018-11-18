package org.dotwebstack.framework.frontend.http.error;

import static org.dotwebstack.framework.frontend.http.MediaTypes.APPLICATION_PROBLEM_JSON;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.error.ProblemDetails.Builder;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

  //Inject http headers from request
  @Context
  private HttpHeaders httpHeaders;

  @Override
  public Response toResponse(@NonNull WebApplicationException waException) {
    String responseMediaType = APPLICATION_PROBLEM_JSON;
    if (httpHeaders != null) {
      List<MediaType> acceptedTypes = httpHeaders.getAcceptableMediaTypes();
      if (acceptedTypes.contains(MediaType.TEXT_HTML_TYPE)) {
        responseMediaType = MediaType.TEXT_HTML;
      }
    }
    Status status = Status.fromStatusCode(waException.getResponse().getStatus());
    ProblemDetails problemDetails = createProblemDetails(waException, status);

    return Response //
        .status(status) //
        .entity(problemDetails) //
        .type(responseMediaType) //
        .build();
  }

  private ProblemDetails createProblemDetails(WebApplicationException waException, Status status) {
    Builder builder = ProblemDetails.builder()//
        .withStatus(status.getStatusCode())//
        .withTitle(status.getReasonPhrase())//
        .withDetail(waException.getMessage());
    if (waException instanceof InvalidParamsBadRequestException) {
      builder.withInvalidParameters(
          ((InvalidParamsBadRequestException) waException).getExtendedDetails());
    }

    return builder.build();
  }

}
