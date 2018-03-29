package org.dotwebstack.framework.transaction.flow.step.validation;

import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.FileConfigurationBackend;
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

  private Backend backend;

  private BackendResourceProvider backendResourceProvider;

  private FileConfigurationBackend fileConfigurationBackend;

  private ValidationStep(@NonNull Builder builder) {
    identifier = builder.identifier;
    conformsTo = builder.conformsTo;
    label = builder.label;
    backend = builder.backend;
    backendResourceProvider = builder.backendResourceProvider;
    fileConfigurationBackend = builder.fileConfigurationBackend;
  }

  @Override
  public StepExecutor createStepExecutor(RepositoryConnection transactionRepositoryConnection) {
    Model transactionModel =
        QueryResults.asModel(transactionRepositoryConnection.getStatements(null, null, null));
    return backendResourceProvider.get(backend.getIdentifier()).createValidationStepExecutor(this,
        transactionModel, fileConfigurationBackend);
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

    private Backend backend;

    private BackendResourceProvider backendResourceProvider;

    private FileConfigurationBackend fileConfigurationBackend;

    public Builder(@NonNull Resource identifier,
        @NonNull BackendResourceProvider backendResourceProvider) {
      this.identifier = identifier;
      this.backendResourceProvider = backendResourceProvider;
    }

    public Builder conformsTo(@NonNull IRI conformsTo) {
      this.conformsTo = conformsTo;
      return this;
    }

    public Builder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    public Builder backend(@NonNull Backend backend) {
      this.backend = backend;
      return this;
    }

    public Builder fileConfigurationBackend(
        @NonNull FileConfigurationBackend fileConfigurationBackend) {
      this.fileConfigurationBackend = fileConfigurationBackend;
      return this;
    }

    public ValidationStep build() {
      return new ValidationStep(this);
    }

  }

}
