package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class SparqlBackend implements Backend {

  private IRI identifier;

  private SPARQLRepository repository;

  private SparqlBackendInformationProductFactory informationProductFactory;

  private RepositoryConnection repositoryConnection;

  private SparqlBackend(Builder builder) {
    identifier = builder.identifier;
    repository = builder.repository;
    informationProductFactory = builder.informationProductFactory;
  }

  @Override
  public IRI getIdentifier() {
    return identifier;
  }

  @Override
  public InformationProduct decorate(InformationProduct informationProduct, Model statements) {
    return informationProductFactory.create(informationProduct, this, statements);
  }

  public SPARQLRepository getRepository() {
    return repository;
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

    private SparqlBackendInformationProductFactory informationProductFactory;

    public Builder(IRI identifier, SPARQLRepository repository,
        SparqlBackendInformationProductFactory informationProductFactory) {
      this.identifier = Objects.requireNonNull(identifier);
      this.repository = Objects.requireNonNull(repository);
      this.informationProductFactory = Objects.requireNonNull(informationProductFactory);
    }

    public SparqlBackend build() {
      return new SparqlBackend(this);
    }

  }

}
