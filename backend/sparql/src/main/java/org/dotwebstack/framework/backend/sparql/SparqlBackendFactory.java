package org.dotwebstack.framework.backend.sparql;

import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendFactory;
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

  @Autowired
  public SparqlBackendFactory(
      @NonNull SparqlBackendInformationProductFactory informationProductFactory) {
    this.informationProductFactory = informationProductFactory;
  }

  @Override
  public Backend create(Model backendModel, Resource identifier) {
    Literal endpoint =
        Models.objectLiteral(backendModel.filter(identifier, ELMO.ENDPOINT, null)).orElseThrow(
            () -> new ConfigurationException(String.format(
                "No <%s> statement has been found for backend <%s>.", ELMO.ENDPOINT, identifier)));

    if (!XMLSchema.ANYURI.equals(endpoint.getDatatype())) {
      throw new ConfigurationException(
          String.format("Object <%s> for backend <%s> must be of datatype <%s>.", ELMO.ENDPOINT,
              identifier, XMLSchema.ANYURI));
    }

    SPARQLRepository repository = new SPARQLRepository(endpoint.stringValue());

    repository.initialize();

    return new SparqlBackend.Builder(identifier, repository, informationProductFactory).build();
  }

  @Override
  public boolean supports(IRI backendType) {
    return backendType.equals(ELMO.SPARQL_BACKEND);
  }

}
