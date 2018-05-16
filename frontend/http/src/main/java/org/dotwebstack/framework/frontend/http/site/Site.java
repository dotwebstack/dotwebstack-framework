package org.dotwebstack.framework.frontend.http.site;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.layout.Layout;
import org.eclipse.rdf4j.model.Resource;

public class Site {

  private Resource identifier;

  private String domain = null;

  private Layout layout;

  private String basePath;

  private Site(Builder builder) {
    this.domain = builder.domain;
    this.identifier = builder.identifier;
    this.layout = builder.layout;
    this.basePath = builder.basePath;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public String getDomain() {
    return domain;
  }

  public Boolean isMatchAllDomain() {
    return domain == null;
  }

  public Layout getLayout() {
    return layout;
  }

  public String getBasePath() {
    return basePath;
  }

  public static class Builder {

    private Resource identifier;

    // Default is match all domain
    private String domain = null;

    private Layout layout;

    private String basePath;

    public Builder(@NonNull Resource identifier) {
      this.identifier = identifier;
    }

    public Builder domain(@NonNull String domain) {
      this.domain = domain;
      return this;
    }

    public Site build() {
      return new Site(this);
    }

    public Builder layout(@NonNull Layout layout) {
      this.layout = layout;
      return this;
    }

    public Builder basePath(@NonNull String basePath) {
      this.basePath = basePath;
      return this;
    }

  }

}
