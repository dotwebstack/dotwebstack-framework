package org.dotwebstack.framework.transaction.flow;

import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SequentialFlowFactory implements FlowFactory {

  private SequentialFlowResourceProvider sequentialFlowResourceProvider;

  @Autowired
  public SequentialFlowFactory(SequentialFlowResourceProvider sequentialFlowResourceProvider) {
    this.sequentialFlowResourceProvider = sequentialFlowResourceProvider;
  }

  @Override
  public boolean supports(IRI flowType) {
    return flowType.equals(ELMO.SEQUENTIAL_FLOW_PROP);
  }

  @Override
  public Flow getResource(Resource identifier) {
    return sequentialFlowResourceProvider.get(identifier);
  }

}
