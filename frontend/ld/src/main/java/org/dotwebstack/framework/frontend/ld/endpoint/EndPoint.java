package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.eclipse.rdf4j.model.Resource;

public class EndPoint {

  private Resource identifier;

  private String pathPattern;

  private String label;

  private Stage stage;

  protected EndPoint(Builder builder) {
    identifier = builder.identifier;
    pathPattern = builder.pathPattern;
    label = builder.label;
    stage = builder.stage;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
  }

  public Stage getStage() {
    return stage;
  }

  public String getPathPattern() {
    return pathPattern;
  }

  public static class Builder {

    private Resource identifier;

    private String pathPattern;

    private String label;

    private Stage stage;

    public Builder(@NonNull Resource identifier, @NonNull String pathPattern) {
      this.identifier = identifier;
      this.pathPattern = pathPattern;
    }

    public Builder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    public Builder stage(@NonNull Stage stage) {
      this.stage = stage;
      return this;
    }

    public EndPoint build() {
      return new EndPoint(this);
    }

  }

}
