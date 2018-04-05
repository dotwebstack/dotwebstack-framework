package org.dotwebstack.framework.backend.sparql.validationstep;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.validation.ValidationStep;
import org.dotwebstack.framework.validation.ShaclValidationException;
import org.dotwebstack.framework.validation.ShaclValidator;
import org.dotwebstack.framework.validation.ValidationReport;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResults;

public class ValidationStepExecutor extends AbstractStepExecutor<ValidationStep> {

  private FileConfigurationBackend backend;

  private Model transactionModel;

  public ValidationStepExecutor(@NonNull ValidationStep validationStep,
      @NonNull Model transactionModel, @NonNull FileConfigurationBackend backend) {
    super(validationStep);
    this.backend = backend;
    this.transactionModel = transactionModel;
  }

  @Override
  public void execute(Collection<Parameter> parameters, Map<String, String> parameterValues) {
    final Model validationModel;

    try {
      validationModel =
          QueryResults.asModel(backend.getRepository().getConnection().getStatements(null, null,
              null, step.getConformsTo()));
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
