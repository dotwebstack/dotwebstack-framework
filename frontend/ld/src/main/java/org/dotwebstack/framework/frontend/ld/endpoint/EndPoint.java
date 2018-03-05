package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.eclipse.rdf4j.model.Resource;

public abstract class EndPoint {

  private Resource identifier;

  private String pathPattern;

  private String label;

  private Stage stage;

  protected EndPoint(EndPointBuilder builder) {
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

  protected static class EndPointBuilder {

    private Resource identifier;

    private String pathPattern;

    private String label;

    private Stage stage;

    public EndPointBuilder(@NonNull Resource identifier, @NonNull String pathPattern) {
      this.identifier = identifier;
      this.pathPattern = pathPattern;
    }

    public EndPointBuilder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    public EndPointBuilder stage(@NonNull Stage stage) {
      this.stage = stage;
      return this;
    }

  }

}
