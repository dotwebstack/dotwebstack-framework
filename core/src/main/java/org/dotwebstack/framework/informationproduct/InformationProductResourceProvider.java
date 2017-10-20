package org.dotwebstack.framework.informationproduct;

import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.filter.FilterResourceProvider;
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

  private final BackendResourceProvider backendResourceProvider;

  private final FilterResourceProvider filterResourceProvider;

  @Autowired
  public InformationProductResourceProvider(ConfigurationBackend configurationBackend,
      @NonNull BackendResourceProvider backendResourceProvider,
      @NonNull FilterResourceProvider filterResourceProvider,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);

    this.backendResourceProvider = backendResourceProvider;
    this.filterResourceProvider = filterResourceProvider;
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
                String.format("No <%s> statement has been found for information product <%s>.",
                    ELMO.BACKEND_PROP, identifier)));
    IRI filterIRI =
        Models.objectIRI(model.filter(identifier, ELMO.PARAMETER_PROP, null)).orElse(null);

    String label = getObjectString(model, identifier, RDFS.LABEL).orElse(null);
    return create(backendIRI, filterIRI, identifier, label, model);
  }

  private InformationProduct create(IRI backendIdentifier, IRI filterIdentifier, IRI identifier,
      String label, Model statements) {
    Backend backend = backendResourceProvider.get(backendIdentifier);
    Filter filter = filterIdentifier != null ? filterResourceProvider.get(filterIdentifier) : null;

    return backend.createInformationProduct(identifier, label, filter, statements);
  }

}
