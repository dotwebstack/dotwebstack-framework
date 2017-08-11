package org.dotwebstack.framework.backend.sparql;

import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendFactory;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Service;

@Service
class SparqlBackendFactory implements BackendFactory {

  @Override
  public Backend create(Model backendModel, IRI identifier) {
    Literal endpoint =
        Models.objectLiteral(backendModel.filter(identifier, ELMO.ENDPOINT, null)).orElseThrow(
            () -> new ConfigurationException(String.format(
                "No <%s> statement has been found for backend <%s>.", ELMO.ENDPOINT, identifier)));

    if (endpoint.getDatatype() != XMLSchema.ANYURI) {
      throw new ConfigurationException(
          String.format("Object <%s> for backend <%s> must be of datatype <%s>.", ELMO.ENDPOINT,
              identifier, XMLSchema.ANYURI));
    }

    return new SparqlBackend.Builder(identifier, endpoint.stringValue()).build();
  }

  @Override
  public boolean supports(IRI backendType) {
    return backendType.equals(ELMO.SPARQL_BACKEND);
  }

}
