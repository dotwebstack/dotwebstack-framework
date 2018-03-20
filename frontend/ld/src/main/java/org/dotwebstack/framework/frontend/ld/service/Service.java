package org.dotwebstack.framework.frontend.ld.service;

import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.model.Resource;

public class Service extends Representation {

  private Service(Builder builder) {
    super(builder);
  }

  public static class Builder extends Representation.Builder<Builder> {

    public Builder(Resource identifier) {
      super(identifier);
    }

    public Builder(Representation representation) {
      super(representation);
    }

    public Service build() {
      return new Service(this);
    }
  }
}
