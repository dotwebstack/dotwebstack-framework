package org.dotwebstack.framework.frontend.ld.redirection;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.eclipse.rdf4j.model.IRI;

public class Redirection {

  private IRI identifier;

  private Stage stage;

  private String urlPattern;

  private String targetUrl;

  private Redirection(Builder builder) {
    identifier = builder.identifier;
    stage = builder.stage;
    urlPattern = builder.urlPattern;
    targetUrl = builder.targetUrl;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public Stage getStage() {
    return stage;
  }

  public String getUrlPattern() {
    return urlPattern;
  }

  public String getTargetUrl() {
    return targetUrl;
  }

  public static class Builder {

    private IRI identifier;

    private Stage stage;

    private String urlPattern;

    private String targetUrl;

    public Builder(@NonNull IRI identifier, @NonNull Stage stage, @NonNull String urlPattern,
        @NonNull String targetUrl) {
      this.identifier = identifier;
      this.stage = stage;
      this.urlPattern = urlPattern;
      this.targetUrl = targetUrl;
    }

    public Redirection build() {
      return new Redirection(this);
    }
  }

}
