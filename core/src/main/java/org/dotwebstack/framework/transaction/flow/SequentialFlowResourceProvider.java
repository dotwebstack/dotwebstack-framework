package org.dotwebstack.framework.transaction.flow;

import java.util.ArrayList;
import java.util.List;
import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelException;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SequentialFlowResourceProvider extends AbstractResourceProvider<SequentialFlow> {

  private StepResourceProvider stepResourceProvider;

  @Autowired
  public SequentialFlowResourceProvider(
      ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties,
      StepResourceProvider stepResourceProvider) {
    super(configurationBackend, applicationProperties);
    this.stepResourceProvider = stepResourceProvider;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    final String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o. ?transaction ?flow ?s. }";

    final GraphQuery graphQuery = conn.prepareGraphQuery(query);
    graphQuery.setBinding("flow", ELMO.SEQUENTIAL_FLOW_PROP);

    return graphQuery;
  }

  @Override
  public SequentialFlow createResource(Model model, Resource identifier) {

    List<Value> stepIris = null;

    try {
      stepIris = RDFCollections.asValues(model, identifier, new ArrayList<Value>());
    } catch (ModelException modelException) {
      throw new ConfigurationException(
          String.format("No steps have been found for flow <%s>.", identifier));
    }

    List<Step> stepList = new ArrayList<>();
    stepIris.forEach(stepIri -> {
      Step step = stepResourceProvider.get((IRI)stepIri);
      if (step == null) {
        throw new ConfigurationException(
            String.format("No step definition <%s> found for flow <%s>.", stepIri, identifier));
      } else {
        stepList.add(step);
      }
    });

    SequentialFlow.Builder sequentialFlowBuilder =
        new SequentialFlow.Builder(identifier, stepList);

    return sequentialFlowBuilder.build();
  }

}
