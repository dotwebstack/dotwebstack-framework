package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.Map;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.frontend.http.error.ExtendedProblemDetailException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class RequestValidationException extends ExtendedProblemDetailException {

  private static final long serialVersionUID = -2378432429849852636L;

  public RequestValidationException(String message, Status status,
      Map<String, Object> extendedDetails) {
    super(message, status, extendedDetails);
  }



}
