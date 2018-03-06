package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.eclipse.rdf4j.model.Resource;

public abstract class EndPoint {

  private Resource identifier;

  private String pathPattern;

  private String label;

  private Stage stage;

  protected EndPoint(EndPointBuilder<?> builder) {
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

  public abstract static class EndPointBuilder<E extends EndPointBuilder<E>> {

    private Resource identifier;

    private String pathPattern;

    private String label;

    private Stage stage;

    protected EndPointBuilder(@NonNull Resource identifier, @NonNull String pathPattern) {
      this.identifier = identifier;
      this.pathPattern = pathPattern;
    }

    protected EndPointBuilder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    protected EndPointBuilder stage(@NonNull Stage stage) {
      this.stage = stage;
      return this;
    }

    protected abstract EndPoint build();
  }

}
