package org.dotwebstack.framework.transaction.flow.step.persistence;

import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class PersistenceStepResourceProvider extends AbstractResourceProvider<PersistenceStep> {

  private BackendResourceProvider backendResourceProvider;

  public PersistenceStepResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    final String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . ?s a ?type . }";

    final GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("type", ELMO.PERSISTENCE_STEP);

    return graphQuery;
  }

  @Override
  protected PersistenceStep createResource(Model model, IRI identifier) {
    PersistenceStep.Builder builder = new PersistenceStep.Builder(identifier);
    getObjectIRI(model, identifier, ELMO.PERSISTENCE_STRATEGY_PROP).ifPresent(
        builder::persistenceStrategy);
    getObjectIRI(model, identifier, ELMO.BACKEND_PROP).ifPresent(
        iri -> builder.backend(backendResourceProvider.get(iri)));
    getObjectIRI(model, identifier, ELMO.TARGET_GRAPH_PROP).ifPresent(builder::targetGraph);

    return builder.build();
  }
}
