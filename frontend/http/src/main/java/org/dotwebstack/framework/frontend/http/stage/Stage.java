package org.dotwebstack.framework.frontend.http.stage;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.eclipse.rdf4j.model.IRI;

public class Stage {

  public static final String DEFAULT_BASE_PATH = "/";

  public static final String PATH_DOMAIN_PARAMETER = "{DOMAIN_PARAMETER}";

  private IRI identifier;

  private Site site;

  private String basePath;

  private Stage(Builder builder) {
    this.identifier = builder.identifier;
    this.site = builder.site;
    this.basePath = builder.basePath;
  }

  public IRI getIdentifier() {
    return identifier;
  }

  public Site getSite() {
    return site;
  }

  public String getBasePath() {
    return basePath;
  }

  public String getFullPath() {
    if (getSite().isMatchAllDomain()) {
      return "/" + PATH_DOMAIN_PARAMETER + getBasePath();
    }

    return "/" + getSite().getDomain() + getBasePath();
  }

  public static class Builder {

    private IRI identifier;

    private Site site;

    private String basePath = DEFAULT_BASE_PATH;

    public Builder(@NonNull IRI identifier, @NonNull Site site) {
      this.identifier = identifier;
      this.site = site;
    }

    public Builder basePath(@NonNull String basePath) {
      this.basePath = basePath;
      return this;
    }

    public Stage build() {
      return new Stage(this);
    }

  }

}
