package org.dotwebstack.framework.transaction.flow.step.validation;

import lombok.NonNull;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class ValidationStep implements Step {

  private Resource identifier;

  private IRI conformsTo;

  private String label;

  private ValidationStep(@NonNull Builder builder) {
    identifier = builder.identifier;
    conformsTo = builder.conformsTo;
    label = builder.label;
  }

  @Override
  public StepExecutor createStepExecutor(RepositoryConnection repositoryConnection) {
    return null;
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

  public static class Builder {

    private Resource identifier;

    private IRI conformsTo;

    private String label;

    public Builder(@NonNull Resource identifier) {
      this.identifier = identifier;
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
