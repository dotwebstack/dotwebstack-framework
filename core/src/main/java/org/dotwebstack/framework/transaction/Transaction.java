package org.dotwebstack.framework.transaction;

import java.util.Collection;
import lombok.NonNull;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.Flow;
import org.eclipse.rdf4j.model.Resource;

public class Transaction {

  private Resource identifier;

  private Flow flow;

  private final Collection<Parameter> parameters;

  public Transaction(@NonNull Builder builder) {
    identifier = builder.identifier;
    flow = builder.flow;
    parameters = builder.parameters;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public Flow getFlow() {
    return flow;
  }

  public Collection<Parameter> getParameters() {
    return parameters;
  }

  public static final class Builder {

    private Resource identifier;

    private Flow flow;

    private Collection<Parameter> parameters;

    public Builder(@NonNull Resource identifier) {
      this.identifier = identifier;
    }

    public Builder flow(@NonNull Flow flow) {
      this.flow = flow;
      return this;
    }

    public Builder parameters(@NonNull Collection<Parameter> parameters) {
      this.parameters = parameters;
      return this;
    }

    public Transaction build() {
      return new Transaction(this);
    }
  }

}
