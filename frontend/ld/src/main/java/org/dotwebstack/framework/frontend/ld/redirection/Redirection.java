package org.dotwebstack.framework.frontend.ld.redirection;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.eclipse.rdf4j.model.Resource;

public class Redirection {

  private Resource identifier;

  private Stage stage;

  private String pathPattern;

  private String redirectTemplate;

  private Redirection(Builder builder) {
    identifier = builder.identifier;
    stage = builder.stage;
    pathPattern = builder.pathPattern;
    redirectTemplate = builder.redirectTemplate;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public Stage getStage() {
    return stage;
  }

  public String getPathPattern() {
    return pathPattern;
  }

  public String getRedirectTemplate() {
    return redirectTemplate;
  }

  public static class Builder {

    private Resource identifier;

    private Stage stage;

    private String pathPattern;

    private String redirectTemplate;

    public Builder(@NonNull Resource identifier, @NonNull Stage stage, @NonNull String pathPattern,
        @NonNull String redirectTemplate) {
      this.identifier = identifier;
      this.stage = stage;
      this.pathPattern = pathPattern;
      this.redirectTemplate = redirectTemplate;
    }

    public Redirection build() {
      return new Redirection(this);
    }
  }

}
