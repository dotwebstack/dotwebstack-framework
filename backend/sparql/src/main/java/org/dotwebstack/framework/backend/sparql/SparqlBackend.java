package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendSourceFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class SparqlBackend implements Backend {

  private IRI identifier;

  private SPARQLRepository repository;

  private SparqlBackendSourceFactory sourceFactory;

  private RepositoryConnection repositoryConnection;

  private SparqlBackend(Builder builder) {
    identifier = builder.identifier;
    repository = builder.repository;
    sourceFactory = builder.sourceFactory;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  public SPARQLRepository getRepository() {
    return repository;
  }

  @Override
  public BackendSourceFactory getSourceFactory() {
    return sourceFactory;
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

    private SparqlBackendSourceFactory sourceFactory;

    public Builder(IRI identifier, SPARQLRepository repository,
        SparqlBackendSourceFactory sourceFactory) {
      this.identifier = Objects.requireNonNull(identifier);
      this.repository = Objects.requireNonNull(repository);
      this.sourceFactory = Objects.requireNonNull(sourceFactory);
    }

    public SparqlBackend build() {
      return new SparqlBackend(this);
    }

  }

}
