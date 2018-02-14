package org.dotwebstack.framework.backend;

import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BackendResourceProvider extends AbstractResourceProvider<Backend> {

  private List<BackendFactory> backendFactories;

  @Autowired
  public BackendResourceProvider(ConfigurationBackend configurationBackend,
      @NonNull List<BackendFactory> backendFactories, ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
    this.backendFactories = backendFactories;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    String query =
        "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . ?type rdfs:subClassOf ?backend }";
    GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("backend", ELMO.BACKEND);

    return graphQuery;
  }

  @Override
  protected Backend createResource(Model model, Resource identifier) {
    IRI backendType = getObjectIRI(model, identifier, RDF.TYPE).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for backend <%s>.", RDF.TYPE, identifier)));

    for (BackendFactory backendFactory : backendFactories) {
      if (backendFactory.supports(backendType)) {
        return backendFactory.create(model, identifier);
      }
    }

    throw new ConfigurationException(
        String.format("No backend factory available for type <%s>.", backendType));
  }

}
