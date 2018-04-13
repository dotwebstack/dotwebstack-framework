package org.dotwebstack.framework.frontend.http.error;

import static org.dotwebstack.framework.frontend.http.MediaTypes.APPLICATION_PROBLEM_JSON;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.error.ProblemDetails.Builder;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

  @Override
  public Response toResponse(@NonNull WebApplicationException waException) {
    Status status = Status.fromStatusCode(waException.getResponse().getStatus());
    ProblemDetails problemDetails = createProblemDetails(waException, status);

    return Response //
        .status(status) //
        .entity(problemDetails) //
        .type(APPLICATION_PROBLEM_JSON) //
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
