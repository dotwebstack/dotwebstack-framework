package org.dotwebstack.framework.frontend.http.error;

import static org.dotwebstack.framework.frontend.http.MediaTypes.APPLICATION_PROBLEM_JSON;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.NonNull;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

  @Override
  public Response toResponse(@NonNull WebApplicationException e) {


    Status status = Status.fromStatusCode(e.getResponse().getStatus());;
    ProblemDetails problemDetails = createProblemDetails(e, status);


    return Response //
        .status(status) //
        .entity(problemDetails) //
        .type(APPLICATION_PROBLEM_JSON) //
        .build();
  }

  private ProblemDetails createProblemDetails(WebApplicationException e, Status status) {
    ProblemDetails problemDetails = new ProblemDetails();

    problemDetails.setStatus(status.getStatusCode());
    problemDetails.setTitle(status.getReasonPhrase());

    problemDetails.setDetail(e.getMessage());

    if (e instanceof ExtendedProblemDetailException) {
      problemDetails.setExtendedDetails(((ExtendedProblemDetailException) e).getExtendedDetails());
    }

    return problemDetails;
  }
}
