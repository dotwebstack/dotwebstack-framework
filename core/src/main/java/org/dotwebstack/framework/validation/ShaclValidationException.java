package org.dotwebstack.framework.validation;

public class ShaclValidationException extends RuntimeException {

  private static final long serialVersionUID = 6908579800668544739L;

  public ShaclValidationException(String message) {
    super(message);
  }

  public ShaclValidationException(String message, Throwable cause) {
    super(message, cause);
  }

}
