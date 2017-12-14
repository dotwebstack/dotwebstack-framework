package org.dotwebstack.framework.validation;

import lombok.NonNull;

public class Violation {

  private final String focusNode;

  private final String resultMessage;

  private final String resultPath;

  public Violation(@NonNull String focusNode, @NonNull String resultMessage,
      @NonNull String resultPath) {
    this.focusNode = focusNode;
    this.resultMessage = resultMessage;
    this.resultPath = resultPath;
  }

  public String getFocusNode() {
    return focusNode;
  }

  public String getMessage() {
    return resultMessage;
  }

  public String getPath() {
    return resultPath;
  }

  public String getReport() {
    return String.format("Invalid configuration at path [%s] on node [%s] with error message [%s]",
        getPath(), getFocusNode(), getMessage());
  }
}
