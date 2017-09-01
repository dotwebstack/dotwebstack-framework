package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class SparqlBackend implements Backend {

  private IRI identifier;

  private SPARQLRepository repository;

  private RepositoryConnection repositoryConnection;

  private SparqlBackend(Builder builder) {
    identifier = builder.identifier;
    repository = builder.repository;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  public SPARQLRepository getRepository() {
    return repository;
  }

  @Override
  public BackendSource createSource(Model model) {
    String query = Models.objectString(model.filter(null, ELMO.QUERY, null)).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for backend source <%s>.", ELMO.QUERY, identifier)));

    return new SparqlBackendSource.Builder(this, query).build();
  }

  public RepositoryConnection getConnection() {
    if (repositoryConnection == null) {
      repositoryConnection = repository.getConnection();
    }

    return repositoryConnection;
  }

  public static class Builder {

    private IRI identifier;

    private SPARQLRepository repository;

    public Builder(IRI identifier, SPARQLRepository repository) {
      this.identifier = Objects.requireNonNull(identifier);
      this.repository = Objects.requireNonNull(repository);
    }

    public SparqlBackend build() {
      return new SparqlBackend(this);
    }

  }

}
