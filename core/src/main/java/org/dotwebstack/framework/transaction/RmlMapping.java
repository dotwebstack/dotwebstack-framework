package org.dotwebstack.framework.transaction;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public class RmlMapping {

  private Resource identifier;

  private Model model;

  public RmlMapping(@NonNull Builder builder) {
    this.identifier = builder.identifier;
    this.model = builder.model;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public Model getModel() {
    return model;
  }

  public static final class Builder {

    private Resource identifier;

    private Model model;

    public Builder(@NonNull Resource identifier, @NonNull Model model) {
      this.identifier = identifier;
      this.model = model;
    }

    public RmlMapping build() {
      return new RmlMapping(this);
    }
  }

}
