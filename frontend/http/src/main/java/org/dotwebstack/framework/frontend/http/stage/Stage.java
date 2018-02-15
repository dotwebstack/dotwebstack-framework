package org.dotwebstack.framework.frontend.http.stage;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.layout.Layout;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.eclipse.rdf4j.model.Resource;

public class Stage {

  public static final String DEFAULT_BASE_PATH = "/";

  public static final String PATH_DOMAIN_PARAMETER = "{DOMAIN_PARAMETER}";

  private Resource identifier;

  private Site site;

  private String basePath;

  private Layout layout;

  private String title;

  private Stage(Builder builder) {
    this.identifier = builder.identifier;
    this.site = builder.site;
    this.basePath = builder.basePath;
    this.layout = builder.layout;
    this.title = builder.title;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public Site getSite() {
    return site;
  }

  public String getBasePath() {
    return basePath;
  }

  public String getTitle() {
    return title;
  }

  public String getFullPath() {
    if (getSite().isMatchAllDomain()) {
      return "/" + PATH_DOMAIN_PARAMETER + getBasePath();
    }

    return "/" + getSite().getDomain() + getBasePath();
  }

  public Layout getLayout() {
    return layout;
  }

  public static class Builder {

    private Resource identifier;

    private Site site;

    private String basePath = DEFAULT_BASE_PATH;

    private Layout layout;

    private String title;

    public Builder(@NonNull Resource identifier, @NonNull Site site) {
      this.identifier = identifier;
      this.site = site;
    }

    public Builder basePath(@NonNull String basePath) {
      this.basePath = basePath;
      return this;
    }

    public Builder title(@NonNull String title) {
      this.title = title;
      return this;
    }

    public Stage build() {
      return new Stage(this);
    }

    public Builder layout(@NonNull Layout layout) {
      this.layout = layout;
      return this;
    }

  }

}
