package org.dotwebstack.framework.transaction.flow.step.update;

import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class UpdateStep implements Step {

  private Resource identifier;

  private String label;

  private String query;

  private Resource backendIri;

  private BackendResourceProvider backendResourceProvider;

  public UpdateStep(@NonNull Builder builder) {
    this.identifier = builder.identifier;
    this.label = builder.label;
    this.query = builder.query;
    this.backendIri = builder.backendIri;
    this.backendResourceProvider = builder.backendResourceProvider;
  }

  public StepExecutor createStepExecutor(@NonNull RepositoryConnection
      transactionRepositoryConnection) {
    if (backendIri.equals(ELMO.TRANSACTION_REPOSITORY)) {
      return new UpdateTransactionRepositoryExecutor(this, transactionRepositoryConnection);
    } else {
      return backendResourceProvider.get(backendIri).createUpdateStepExecutor(this);
    }
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
  }

  public String getQuery() {
    return query;
  }

  public Resource getBackendIri() {
    return backendIri;
  }

  public static final class Builder {

    private Resource identifier;

    private String label;

    private String query;

    private Resource backendIri;

    private BackendResourceProvider backendResourceProvider;

    public Builder(@NonNull Resource identifier,
        @NonNull BackendResourceProvider backendResourceProvider) {
      this.identifier = identifier;
      this.backendResourceProvider = backendResourceProvider;
    }

    public Builder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    public Builder query(@NonNull String query) {
      this.query = query;
      return this;
    }

    public Builder backend(@NonNull Resource backendIri) {
      this.backendIri = backendIri;
      return this;
    }

    public UpdateStep build() {
      return new UpdateStep(this);
    }
  }

}
