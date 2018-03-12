package org.dotwebstack.framework.transaction;

import lombok.NonNull;
import org.dotwebstack.framework.transaction.flow.Flow;
import org.eclipse.rdf4j.model.Resource;

public class Transaction {

  private Resource identifier;

  private Flow flow;

  public Transaction(@NonNull Builder builder) {
    identifier = builder.identifier;
    flow = builder.flow;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public Flow getFlow() {
    return flow;
  }

  public static final class Builder {

    private Resource identifier;

    private Flow flow;

    public Builder(@NonNull Resource identifier) {
      this.identifier = identifier;
    }

    public Builder flow(@NonNull Flow flow) {
      this.flow = flow;
      return this;
    }

    public Transaction build() {
      return new Transaction(this);
    }
  }

}
