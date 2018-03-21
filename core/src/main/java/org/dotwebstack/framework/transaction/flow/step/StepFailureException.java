package org.dotwebstack.framework.transaction.flow.step;

public final class StepFailureException extends RuntimeException {

  private static final long serialVersionUID = -6994374606766886595L;

  public StepFailureException(String message) {
    super(message);
  }

  public StepFailureException(String message, Throwable cause) {
    super(message, cause);
  }

}
