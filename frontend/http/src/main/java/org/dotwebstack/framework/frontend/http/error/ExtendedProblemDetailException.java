package org.dotwebstack.framework.frontend.http.error;

import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class ExtendedProblemDetailException extends WebApplicationException {

  private static final long serialVersionUID = 4523810251268331255L;

  private final transient Map<String, Object> extendedDetails;

  public ExtendedProblemDetailException(String message, Status status,
      Map<String, Object> extendedDetails) {
    super(message, status);
    this.extendedDetails = extendedDetails;
  }

  Map<String, Object> getExtendedDetails() {
    return extendedDetails;
  }

}
