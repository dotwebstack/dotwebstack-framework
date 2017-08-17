package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendSource;

public class SparqlBackendSource implements BackendSource {

  private Backend backend;

  private String query;

  public SparqlBackendSource(Builder builder) {
    this.backend = builder.backend;
    this.query = builder.query;
  }

  @Override
  public Backend getBackend() {
    return backend;
  }

  public String getQuery() {
    return query;
  }

  public static class Builder {

    private Backend backend;

    private String query;

    public Builder(Backend backend, String query) {
      this.backend = Objects.requireNonNull(backend);
      this.query = Objects.requireNonNull(query);
    }

    public SparqlBackendSource build() {
      return new SparqlBackendSource(this);
    }

  }
}
