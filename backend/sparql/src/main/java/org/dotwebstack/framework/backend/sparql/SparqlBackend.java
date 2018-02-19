package org.dotwebstack.framework.backend.sparql;

import java.util.Collection;
import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.sparql.informationproduct.SparqlBackendInformationProductFactory;
import org.dotwebstack.framework.backend.sparql.persistencestep.SparqlBackendPersistenceStepFactory;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

public class SparqlBackend implements Backend {

  private Resource identifier;

  private SPARQLRepository repository;

  private SparqlBackendInformationProductFactory informationProductFactory;

  private SparqlBackendPersistenceStepFactory persistenceStepFactory;

  private RepositoryConnection repositoryConnection;

  private SparqlBackend(Builder builder) {
    identifier = builder.identifier;
    repository = builder.repository;
    informationProductFactory = builder.informationProductFactory;
    persistenceStepFactory = builder.persistenceStepFactory;
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

  @Override
  public StepExecutor createPersistenceStepExecutor(PersistenceStep persistenceStep,
      Model transactionModel) {
    return persistenceStepFactory.create(persistenceStep, transactionModel, this);
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

    private SparqlBackendPersistenceStepFactory persistenceStepFactory;

    public Builder(@NonNull Resource identifier, @NonNull SPARQLRepository repository,
        @NonNull SparqlBackendInformationProductFactory informationProductFactory,
        @NonNull SparqlBackendPersistenceStepFactory persistenceStepFactory) {
      this.identifier = identifier;
      this.repository = repository;
      this.informationProductFactory = informationProductFactory;
      this.persistenceStepFactory = persistenceStepFactory;
    }

    public SparqlBackend build() {
      return new SparqlBackend(this);
    }

  }

}
