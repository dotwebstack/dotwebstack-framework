package org.dotwebstack.framework.transaction.flow.step.validation;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.validation.ShaclValidationException;
import org.dotwebstack.framework.validation.ShaclValidator;
import org.dotwebstack.framework.validation.ValidationReport;
import org.eclipse.rdf4j.model.Model;

public class ValidationStepExecutor extends AbstractStepExecutor<ValidationStep> {

  private Model transactionModel;

  public ValidationStepExecutor(@NonNull ValidationStep validationStep,
      @NonNull Model transactionModel) {
    super(validationStep);
    this.transactionModel = transactionModel;
  }

  @Override
  public void execute(Collection<Parameter> parameters, Map<String, String> parameterValues) {
    final ShaclValidator shaclValidator = new ShaclValidator();
    final ValidationReport report =
        shaclValidator.validate(transactionModel, step.getValidationModel());
    if (!report.isValid()) {
      throw new ShaclValidationException(report.printReport());
    }
  }

}
