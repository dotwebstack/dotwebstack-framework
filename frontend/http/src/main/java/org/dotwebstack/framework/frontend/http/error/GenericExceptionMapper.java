package org.dotwebstack.framework.frontend.http.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class);

  @Override
  public Response toResponse(Exception throwable) {

    LOG.error(throwable.getMessage(), throwable);

    Status status = Status.INTERNAL_SERVER_ERROR;
    ProblemDetails problemDetails = createProblemDetails(throwable, status);

    return Response //
        .status(status) //
        .entity(problemDetails) //
        .type(MediaTypes.APPLICATION_PROBLEM_JSON) //
        .build();
  }

  private ProblemDetails createProblemDetails(Exception throwable, Status status) {
    ProblemDetails problemDetails = new ProblemDetails();

    problemDetails.setStatus(status.getStatusCode());
    problemDetails.setTitle(status.getReasonPhrase());
    problemDetails.setDetail(String.format("An error occured from which the server was unable "
        + "to recover. Please contact the system administrator with the following details: '%s'",
        throwable.getMessage()));

    return problemDetails;
  }


}
