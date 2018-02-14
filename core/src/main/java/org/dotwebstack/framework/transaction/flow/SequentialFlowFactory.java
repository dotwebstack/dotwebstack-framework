package org.dotwebstack.framework.transaction.flow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SequentialFlowFactory implements FlowFactory {

  private StepResourceProvider stepResourceProvider;

  @Autowired
  public SequentialFlowFactory(StepResourceProvider stepResourceProvider) {
    this.stepResourceProvider = stepResourceProvider;
  }

  @Override
  public boolean supports(IRI flowType) {
    return flowType.equals(ELMO.SEQUENTIAL_FLOW_PROP);
  }

  @Override
  public SequentialFlow create(Model model, IRI identifier) {

    Collection<IRI> stepIris =
        Models.objectIRIs(model.filter(identifier, ELMO.SEQUENTIAL_FLOW_PROP, null));

    if (stepIris.isEmpty()) {
      throw new ConfigurationException(
          String.format("No <%s> statement has been found for transaction <%s>.",
              ELMO.SEQUENTIAL_FLOW_PROP, identifier));
    }

    List<Step> stepList = new ArrayList<Step>();
    stepIris.forEach(stepIri -> {
      Step step = stepResourceProvider.get(stepIri);
      if (step == null) {
        throw new ConfigurationException(
            String.format("No step definition <%s> found for transaction <%s>.", step, identifier));
      } else {
        stepList.add(step);
      }
    });

    SequentialFlow.Builder sequentialFlowBuilder = new SequentialFlow.Builder(stepList);
    return sequentialFlowBuilder.build();

  }

}
