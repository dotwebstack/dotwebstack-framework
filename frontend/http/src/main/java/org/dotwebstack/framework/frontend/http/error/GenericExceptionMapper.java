package org.dotwebstack.framework.frontend.http.error;

import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericExceptionMapper implements ExceptionMapper<Exception> {

  //Inject http headers from request
  @Context
  private HttpHeaders httpHeaders;

  private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class);

  @Override
  public Response toResponse(Exception cause) {

    String identifier = generateIdentifier();
    if (LOG.isErrorEnabled()) {
      LOG.error(String.format("[%s] %s", identifier, cause.getMessage()), cause);
    }

    String responseMediaType = MediaTypes.APPLICATION_PROBLEM_JSON;
    if (httpHeaders != null) {
      List<MediaType> acceptedTypes = httpHeaders.getAcceptableMediaTypes();
      if (acceptedTypes.contains(MediaType.TEXT_HTML_TYPE)) {
        responseMediaType = MediaType.TEXT_HTML;
      }
    }
    Status status = Status.INTERNAL_SERVER_ERROR;
    ProblemDetails problemDetails = createProblemDetails(status, identifier);

    return Response //
        .status(status) //
        .entity(problemDetails) //
        .type(responseMediaType) //
        .build();
  }

  private String generateIdentifier() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  private ProblemDetails createProblemDetails(Status status, String identifier) {
    return ProblemDetails.builder()//
        .withStatus(status.getStatusCode())//
        .withTitle(status.getReasonPhrase())//
        .withDetail(String.format(
            "An error occured from which the server was unable to recover."
                + "Please contact the system administrator with the following details: '%s'",
            identifier))//
        .build();
  }


}
