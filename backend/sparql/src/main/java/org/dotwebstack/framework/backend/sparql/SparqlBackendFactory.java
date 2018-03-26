package org.dotwebstack.framework.backend.sparql;

import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendFactory;
import org.dotwebstack.framework.backend.sparql.informationproduct.SparqlBackendInformationProductFactory;
import org.dotwebstack.framework.backend.sparql.persistencestep.SparqlBackendPersistenceStepFactory;
import org.dotwebstack.framework.backend.sparql.updatestep.SparqlBackendUpdateStepFactory;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class SparqlBackendFactory implements BackendFactory {

  private SparqlBackendInformationProductFactory informationProductFactory;

  private SparqlBackendPersistenceStepFactory persistenceStepFactory;

  private SparqlBackendUpdateStepFactory updateStepFactory;

  @Autowired
  public SparqlBackendFactory(
      @NonNull SparqlBackendInformationProductFactory informationProductFactory,
      @NonNull SparqlBackendPersistenceStepFactory persistenceStepFactory,
      @NonNull SparqlBackendUpdateStepFactory updateStepFactory) {
    this.informationProductFactory = informationProductFactory;
    this.persistenceStepFactory = persistenceStepFactory;
    this.updateStepFactory = updateStepFactory;
  }

  @Override
  public Backend create(Model backendModel, Resource identifier) {
    Literal endpoint =
        Models.objectLiteral(backendModel.filter(identifier, ELMO.ENDPOINT_PROP, null)).orElseThrow(
            () -> new ConfigurationException(
                String.format("No <%s> statement has been found for backend <%s>.",
                    ELMO.ENDPOINT_PROP, identifier)));

    if (!XMLSchema.ANYURI.equals(endpoint.getDatatype())) {
      throw new ConfigurationException(
          String.format("Object <%s> for backend <%s> must be of datatype <%s>.",
              ELMO.ENDPOINT_PROP, identifier, XMLSchema.ANYURI));
    }

    SPARQLRepository repository = new SPARQLRepository(endpoint.stringValue());

    repository.initialize();

    return new SparqlBackend.Builder(identifier, repository, informationProductFactory,
        persistenceStepFactory, updateStepFactory).build();
  }

  @Override
  public boolean supports(IRI backendType) {
    return backendType.equals(ELMO.SPARQL_BACKEND);
  }

}
