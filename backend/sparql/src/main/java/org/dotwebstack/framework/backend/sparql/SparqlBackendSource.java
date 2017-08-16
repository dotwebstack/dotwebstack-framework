package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.BackendSource;
import org.eclipse.rdf4j.model.IRI;

public class SparqlBackendSource implements BackendSource {

  private IRI backendReference;

  private String query;

  public SparqlBackendSource(Builder builder) {
    this.backendReference = builder.backendReference;
    this.query = builder.query;
  }

  @Override
  public IRI getBackendReference() {
    return backendReference;
  }

  public String getQuery() {
    return query;
  }

  public static class Builder {

    private IRI backendReference;

    private String query;

    public Builder(IRI backendReference, String query) {
      this.backendReference = Objects.requireNonNull(backendReference);
      this.query = Objects.requireNonNull(query);
    }

    public SparqlBackendSource build() {
      return new SparqlBackendSource(this);
    }

  }
}
