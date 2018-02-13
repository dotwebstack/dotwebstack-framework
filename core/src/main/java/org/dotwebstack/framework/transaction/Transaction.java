package org.dotwebstack.framework.transaction;

import lombok.NonNull;
import org.dotwebstack.framework.transaction.flow.Flow;
import org.eclipse.rdf4j.model.IRI;

public class Transaction {

  private IRI identifier;

  private Flow flow;

  public Transaction(Builder builder) {
    identifier = builder.identifier;
    flow = builder.flow;
  }

  public static final class Builder {

    private IRI identifier;
    private Flow flow;

    public Builder(@NonNull IRI identifier) {
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
