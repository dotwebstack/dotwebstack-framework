package org.dotwebstack.framework.site;

import java.util.Objects;

import org.eclipse.rdf4j.model.IRI;

public class Site {
  private static final String MATCH_ALL_DOMAIN = "*";

  private IRI identifier;

  private String domain;

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
    return this.domain.equals(MATCH_ALL_DOMAIN);
  }

  public static class Builder {
    private IRI identifier;

    // Default is match all domain
    private String domain = MATCH_ALL_DOMAIN;

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
