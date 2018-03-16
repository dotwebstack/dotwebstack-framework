package org.dotwebstack.framework.transaction.flow.step.assertion;

import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class AssertionStep implements Step {

  private Resource identifier;

  private String label;

  private String assertionQuery;

  private Boolean isAssertionNot;

  public AssertionStep(@NonNull Builder builder) {
    this.identifier = builder.identifier;
    this.label = builder.label;
    this.assertionQuery = builder.assertionQuery;
    this.isAssertionNot = builder.isAssertionNot;
  }

  public StepExecutor createStepExecutor(
      @NonNull RepositoryConnection transactionRepositoryConnection) {
    return new AssertionTransactionRepositoryExecutor(this,
        transactionRepositoryConnection);
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public String getLabel() {
    return label;
  }

  public String getAssertionQuery() {
    return assertionQuery;
  }

  public Boolean isAssertionNot() {
    return isAssertionNot;
  }

  public static final class Builder {

    private Resource identifier;

    private String label;

    private String assertionQuery;

    private Boolean isAssertionNot;

    public Builder(@NonNull Resource identifier) {
      this.identifier = identifier;
    }

    public Builder label(@NonNull String label) {
      this.label = label;
      return this;
    }

    public Builder assertion(Optional<String> assertionQuery, Optional<String> assertionNotQuery) {
      assertionQuery.ifPresent(query -> {
        this.assertionQuery = query;
        this.isAssertionNot = false;
      });
      assertionNotQuery.ifPresent(query -> {
        this.assertionQuery = query;
        this.isAssertionNot = true;
      });
      return this;
    }

    public AssertionStep build() {
      return new AssertionStep(this);
    }
  }

}
