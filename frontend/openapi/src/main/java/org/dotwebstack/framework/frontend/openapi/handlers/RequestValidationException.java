package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class RequestValidationException extends WebApplicationException {

  private static final long serialVersionUID = -2378432429849852636L;

  private final transient Map<String, Object> details;

  public RequestValidationException(String message, Status status, Map<String, Object> details) {
    super(message, status);
    this.details = details;
  }

  Map<String, Object> getDetails() {
    return details;
  }

}
