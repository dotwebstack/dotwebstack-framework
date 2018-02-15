package org.dotwebstack.framework.backend.sparql;

import java.util.Collection;
import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class SparqlBackend implements Backend {

  private Resource identifier;

  private SPARQLRepository repository;

  private SparqlBackendInformationProductFactory informationProductFactory;

  private RepositoryConnection repositoryConnection;

  private SparqlBackend(Builder builder) {
    identifier = builder.identifier;
    repository = builder.repository;
    informationProductFactory = builder.informationProductFactory;
  }

  @Override
  public Resource getIdentifier() {
    return identifier;
  }

  @Override
  public InformationProduct createInformationProduct(Resource identifier, String label,
      Collection<Parameter> parameters, Model statements) {
    return informationProductFactory.create(identifier, label, this, parameters, statements);
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

    private Resource identifier;

    private SPARQLRepository repository;

    private SparqlBackendInformationProductFactory informationProductFactory;

    public Builder(@NonNull Resource identifier, @NonNull SPARQLRepository repository,
        @NonNull SparqlBackendInformationProductFactory informationProductFactory) {
      this.identifier = identifier;
      this.repository = repository;
      this.informationProductFactory = informationProductFactory;
    }

    public SparqlBackend build() {
      return new SparqlBackend(this);
    }

  }

}
