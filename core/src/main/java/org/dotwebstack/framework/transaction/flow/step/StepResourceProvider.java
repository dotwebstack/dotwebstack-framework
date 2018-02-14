package org.dotwebstack.framework.transaction.flow.step;

import java.util.List;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StepResourceProvider extends AbstractResourceProvider<Step> {

  private List<StepFactory> stepFactoryList;

  @Autowired
  public StepResourceProvider(ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties, List<StepFactory> stepFactoryList) {
    super(configurationBackend, applicationProperties);
    this.stepFactoryList = stepFactoryList;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    final String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o. ?transaction ?flow ?s. }";

    final GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("flow", ELMO.SEQUENTIAL_FLOW_PROP);

    return graphQuery;
  }

  @Override
  protected Step createResource(Model model, IRI identifier) {
    IRI transactionStep = getObjectIRI(model, identifier, RDF.TYPE).orElseThrow(
        () -> new ConfigurationException(String.format(
            "No <%s> statement has been found for appearance <%s>.", RDF.TYPE, identifier)));

    for (StepFactory stepFactory : stepFactoryList) {
      if (stepFactory.supports(transactionStep)) {
        return stepFactory.create(model, identifier);
      }
    }

    throw new ConfigurationException(
        String.format("No transaction step factory available for type <%s>.", transactionStep));
  }

}
