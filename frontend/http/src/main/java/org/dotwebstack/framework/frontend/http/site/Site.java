package org.dotwebstack.framework.frontend.http.site;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.layout.Layout;
import org.eclipse.rdf4j.model.IRI;

public class Site {

  private IRI identifier;

  private String domain = null;

  private Layout layout;

  private Site(Builder builder) {
    this.domain = builder.domain;
    this.identifier = builder.identifier;
    this.layout = builder.layout;
  }

  public IRI getIdentifier() {
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

  public static class Builder {

    private IRI identifier;

    // Default is match all domain
    private String domain = null;

    private Layout layout;

    public Builder(@NonNull IRI identifier) {
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

  }

}
