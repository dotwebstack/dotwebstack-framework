package org.dotwebstack.framework.frontend.http.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

  private static final Logger LOG = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

  @Override
  public Response toResponse(@NonNull WebApplicationException e) {
    LOG.debug("Mapping WebApplicationException: {}", e.getMessage());

    return Response.fromResponse(e.getResponse()).entity(
        e.getResponse().getStatusInfo().getReasonPhrase()).type(MediaType.TEXT_PLAIN_TYPE).build();
  }

}
