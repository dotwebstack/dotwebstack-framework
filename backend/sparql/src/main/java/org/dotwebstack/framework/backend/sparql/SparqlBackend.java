package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.eclipse.rdf4j.model.IRI;

class SparqlBackend implements Backend {

  private IRI identifier;

  private String endpoint;

  private SparqlBackend(Builder builder) {
    identifier = builder.identifier;
    endpoint = builder.endpoint;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public static class Builder {

    private IRI identifier;

    private String endpoint;

    public Builder(IRI identifier, String endpoint) {
      this.identifier = Objects.requireNonNull(identifier);
      this.endpoint = Objects.requireNonNull(endpoint);
    }

    public SparqlBackend build() {
      return new SparqlBackend(this);
    }

  }

}
