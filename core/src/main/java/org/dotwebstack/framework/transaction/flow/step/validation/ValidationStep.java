package org.dotwebstack.framework.transaction.flow.step.validation;

import lombok.NonNull;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class ValidationStep implements Step {

  private Resource identifier;

  private IRI conformsTo;

  private String label;

  private Model validationModel;

  private ValidationStep(@NonNull Builder builder) {
    identifier = builder.identifier;
    conformsTo = builder.conformsTo;
    label = builder.label;
    validationModel = builder.validationModel;
  }

  @Override
  public StepExecutor createStepExecutor(RepositoryConnection transactionRepositoryConnection) {
    Model transactionModel =
        QueryResults.asModel(transactionRepositoryConnection.getStatements(null, null, null));
    return new ValidationStepExecutor(this, transactionModel);
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public IRI getConformsTo() {
    return conformsTo;
  }

  public String getLabel() {
    return label;
  }

  public Model getValidationModel() {
    return validationModel;
  }

  public static class Builder {

    private Resource identifier;

    private IRI conformsTo;

    private String label;

    private Model validationModel;

    public Builder(@NonNull Resource identifier, @NonNull Model validationModel) {
      this.identifier = identifier;
      this.validationModel = validationModel;
    }

    public Builder conformsTo(@NonNull IRI conformsTo) {
      this.conformsTo = conformsTo;
      return this;
    }

    public Builder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    public ValidationStep build() {
      return new ValidationStep(this);
    }

  }

}
