package org.dotwebstack.framework.backend.sparql.validationstep;

import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.backend.sparql.SparqlBackend;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.validation.ValidationStep;
import org.dotwebstack.framework.validation.ShaclValidationException;
import org.dotwebstack.framework.validation.ShaclValidator;
import org.dotwebstack.framework.validation.ValidationReport;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResults;

public class ValidationStepExecutor extends AbstractStepExecutor<ValidationStep> {

  private SparqlBackend backend;

  private Model transactionModel;

  private ValidationStep validationStep;

  private QueryEvaluator queryEvaluator;

  public ValidationStepExecutor(@NonNull ValidationStep validationStep,
      @NonNull Model transactionModel, @NonNull SparqlBackend backend,
      @NonNull QueryEvaluator queryEvaluator) {
    super(validationStep);
    this.backend = backend;
    this.transactionModel = transactionModel;
    this.validationStep = validationStep;
    this.queryEvaluator = queryEvaluator;
  }

  @Override
  public void execute() {
    final Model validationModel;

    try {
      validationModel = QueryResults.asModel(
          backend.getConnection().getStatements(null, null, null, step.getConformsTo()));
    } catch (RDF4JException ex) {
      throw new BackendException(String.format("........: (%s)", ex.getMessage()), ex);
    }

    final ShaclValidator shaclValidator = new ShaclValidator();
    final ValidationReport report = shaclValidator.validate(transactionModel, validationModel);
    if (!report.isValid()) {
      throw new ShaclValidationException(report.printReport());
    }
  }

}
