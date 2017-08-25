package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;

public class SparqlBackend implements Backend {

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

  @Override
  public BackendSource createSource(Model model) {
    String query = Models.objectString(model.filter(null, ELMO.QUERY, null)).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for backend source <%s>.", ELMO.QUERY, identifier)));

    return new SparqlBackendSource.Builder(this, query).build();
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
