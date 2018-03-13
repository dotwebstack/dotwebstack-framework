package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.model.Resource;

public class DirectEndPoint extends AbstractEndPoint {

  private Representation getRepresentation;

  private Representation postRepresentation;

  public DirectEndPoint(Builder builder) {
    super(builder);
    this.getRepresentation = builder.getRepresentation;
    this.postRepresentation = builder.postRepresentation;
  }

  public Representation getGetRepresentation() {
    return getRepresentation;
  }

  public Representation getPostRepresentation() {
    return postRepresentation;
  }

  public static class Builder extends EndPointBuilder<Builder> {

    private Representation getRepresentation;

    private Representation postRepresentation;

    public Builder(@NonNull Resource identifier, @NonNull String pathPattern) {
      super(identifier, pathPattern);
    }

    public Builder getRepresentation(Representation representation) {
      this.getRepresentation = representation;
      return this;
    }

    public Builder postRepresentation(Representation representation) {
      this.postRepresentation = representation;
      return this;
    }

    public DirectEndPoint build() {
      return new DirectEndPoint(this);
    }
  }

}
