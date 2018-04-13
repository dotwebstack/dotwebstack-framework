package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.List;
import org.dotwebstack.framework.frontend.http.error.InvalidParamsBadRequestException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class RequestValidationException extends InvalidParamsBadRequestException {

  private static final long serialVersionUID = -2378432429849852636L;

  public RequestValidationException(String message, List<InvalidParameter> extendedDetails) {
    super(message, extendedDetails);
  }



}
