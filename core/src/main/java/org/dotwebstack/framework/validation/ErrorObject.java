package org.dotwebstack.framework.validation;

import lombok.NonNull;

public class ErrorObject {

  private final String focusNode;

  private final String resultMessage;

  private final String resultPath;

  public ErrorObject(@NonNull String focusNode, @NonNull String resultMessage,
      @NonNull String resultPath) {
    this.focusNode = focusNode;
    this.resultMessage = resultMessage;
    this.resultPath = resultPath;
  }

  public String getFocusNode() {
    return focusNode;
  }

  public String getResultMessage() {
    return resultMessage;
  }

  public String getResultPath() {
    return resultPath;
  }

  public String getErrorReport() {
    return String.format(
        "Invalid configuration at path [%s] on node [%s] with error " + "message [%s]", resultPath,
        focusNode, resultMessage);
  }
}
