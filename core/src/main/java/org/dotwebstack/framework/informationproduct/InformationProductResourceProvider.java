package org.dotwebstack.framework.informationproduct;

import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InformationProductResourceProvider
    extends AbstractResourceProvider<InformationProduct> {

  private BackendResourceProvider backendResourceProvider;

  @Autowired
  public InformationProductResourceProvider(ConfigurationBackend configurationBackend,
      BackendResourceProvider backendResourceProvider) {
    super(configurationBackend);
    this.backendResourceProvider = backendResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.INFORMATION_PRODUCT);
    return graphQuery;
  }

  @Override
  protected InformationProduct createResource(Model model, IRI identifier) {
    IRI backendIRI =
        Models.objectIRI(model.filter(identifier, ELMO.BACKEND_PROP, null)).orElseThrow(
            () -> new ConfigurationException(
                String.format("No <%s> backend has been found for information product <%s>.",
                    ELMO.BACKEND_PROP, identifier)));

    return new InformationProduct.Builder(identifier, createBackendSource(backendIRI, model)).label(
        getObjectString(model, identifier, RDFS.LABEL).orElse(null)).build();
  }

  private BackendSource createBackendSource(IRI backendIdentifier, Model statements) {
    return backendResourceProvider.get(backendIdentifier).createSource(statements);
  }

}
