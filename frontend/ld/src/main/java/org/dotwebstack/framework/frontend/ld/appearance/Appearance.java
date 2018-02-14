package org.dotwebstack.framework.frontend.ld.appearance;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public class Appearance {

  private Resource identifier;

  private IRI type;

  private Model model;

  private Appearance(Builder builder) {
    identifier = builder.identifier;
    type = builder.type;
    model = builder.model;
  }

  public Resource getIdentifier() {
    return identifier;
  }

  public IRI getType() {
    return type;
  }

  public Model getModel() {
    return model;
  }

  public static class Builder {

    private Resource identifier;

    private IRI type;

    private Model model;

    public Builder(@NonNull Resource identifier, @NonNull IRI type, @NonNull Model model) {
      this.identifier = identifier;
      this.type = type;
      this.model = model;
    }

    public Appearance build() {
      return new Appearance(this);
    }

  }

}
