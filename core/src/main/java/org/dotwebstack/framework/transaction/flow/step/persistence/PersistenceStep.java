package org.dotwebstack.framework.transaction.flow.step.persistence;

import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;

public class PersistenceStep implements Step {

  private Resource identifier;

  // todo replace IRI by Strategy
  private IRI persistenceStrategy;

  private Backend backend;

  private IRI targetGraph;

  public PersistenceStep(Builder builder) {
    this.identifier = builder.identifier;
    this.persistenceStrategy = builder.persistenceStrategy;
    this.backend = builder.backend;
    this.targetGraph = builder.targetGraph;
  }

  public static final class Builder {

    private Resource identifier;

    private IRI persistenceStrategy;

    private Backend backend;

    private IRI targetGraph;

    public Builder(@NonNull Resource identifier) {
      this.identifier = identifier;
    }

    public Builder persistenceStrategy(@NonNull IRI persistenceStrategy) {
      this.persistenceStrategy = persistenceStrategy;
      return this;
    }

    public Builder backend(@NonNull Backend backend) {
      this.backend = backend;
      return this;
    }

    public Builder targetGraph(@NonNull IRI targetGraph) {
      this.targetGraph = targetGraph;
      return this;
    }

    public PersistenceStep build() {
      return new PersistenceStep(this);
    }
  }
}
