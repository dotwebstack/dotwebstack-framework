package org.dotwebstack.framework.backend.sparql.validationstep;

import lombok.NonNull;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.dotwebstack.framework.transaction.flow.step.validation.ValidationStep;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendValidationStepFactory {
  private QueryEvaluator queryEvaluator;

  @Autowired
  public SparqlBackendValidationStepFactory(QueryEvaluator queryEvaluator) {
    this.queryEvaluator = queryEvaluator;
  }

  public ValidationStepExecutor create(@NonNull ValidationStep validationStep,
      @NonNull Model transactionModel, @NonNull FileConfigurationBackend fileConfigurationBackend) {
    return new ValidationStepExecutor(validationStep, transactionModel, fileConfigurationBackend,
        queryEvaluator);
  }

}
