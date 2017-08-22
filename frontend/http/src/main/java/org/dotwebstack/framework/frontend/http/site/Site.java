package org.dotwebstack.framework.frontend.http.site;

import java.util.Objects;

import org.eclipse.rdf4j.model.IRI;

public class Site {

  private IRI identifier;

  private String domain = null;

  private Site(Builder builder) {
    this.domain = builder.domain;
    this.identifier = builder.identifier;
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

  public static class Builder {
    private IRI identifier;

    // Default is match all domain
    private String domain = null;

    public Builder(IRI identifier) {
      this.identifier = Objects.requireNonNull(identifier);
    }

    public Builder domain(String domain) {
      this.domain = Objects.requireNonNull(domain);
      return this;
    }

    public Site build() {
      return new Site(this);
    }
  }

}
