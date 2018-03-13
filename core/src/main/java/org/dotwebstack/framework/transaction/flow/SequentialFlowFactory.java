package org.dotwebstack.framework.transaction.flow;

import lombok.NonNull;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SequentialFlowFactory implements FlowFactory {

  private SequentialFlowResourceProvider sequentialFlowResourceProvider;

  @Autowired
  public SequentialFlowFactory(@NonNull SequentialFlowResourceProvider
      sequentialFlowResourceProvider) {
    this.sequentialFlowResourceProvider = sequentialFlowResourceProvider;
  }

  @Override
  public boolean supports(@NonNull IRI flowType) {
    return flowType.equals(ELMO.SEQUENTIAL_FLOW_PROP);
  }

  @Override
  public Flow getResource(@NonNull Resource identifier) {
    return sequentialFlowResourceProvider.get(identifier);
  }

}
