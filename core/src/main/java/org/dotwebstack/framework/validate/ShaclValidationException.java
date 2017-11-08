package org.dotwebstack.framework.validate;

public class ShaclValidationException extends Exception {

  private static final long serialVersionUID = 6908579800668544739L;

  public ShaclValidationException(String message) {
    super(message);
  }

  public ShaclValidationException(String message, Throwable cause) {
    super(message, cause);
  }

}
