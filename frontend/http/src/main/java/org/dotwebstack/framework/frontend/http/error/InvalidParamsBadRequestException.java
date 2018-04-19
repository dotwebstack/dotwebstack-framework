package org.dotwebstack.framework.frontend.http.error;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

public class InvalidParamsBadRequestException extends WebApplicationException {

  private final transient List<InvalidParameter> extendedDetails;

  public InvalidParamsBadRequestException(String message, List<InvalidParameter> extendedDetails) {
    super(message, Status.BAD_REQUEST);
    this.extendedDetails = extendedDetails;
  }

  List<InvalidParameter> getExtendedDetails() {
    return extendedDetails;
  }

  public static class InvalidParameter {
    private String name;

    public String getName() {
      return name;
    }

    public String getReason() {
      return reason;
    }

    private String reason;

    public InvalidParameter(String name, String reason) {
      this.name = name;
      this.reason = reason;
    }
  }

}
