package org.dotwebstack.framework.frontend.ld.service;

import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.model.Resource;

public class Service extends Representation {

  private Service(Builder builder) {
    super(builder);
  }

  public static class Builder extends RepresentationBuilder<Builder> {

    public Builder(Resource identifier) {
      super(identifier);
    }

    public Builder(Representation representation) {
      super(representation);
    }

    @Override
    public Service build() {
      return new Service(this);
    }
  }
}
