package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.model.Resource;

public class DirectEndPoint extends AbstractEndPoint {

  private Representation representationGet;

  private Representation representationPost;

  public DirectEndPoint(Builder builder) {
    super(builder);
    this.representationGet = builder.representationGet;
    this.representationPost = builder.representationPost;
  }

  public Representation getRepresentationGet() {
    return representationGet;
  }

  public Representation getRepresentationPost() {
    return representationPost;
  }

  public static class Builder extends EndPointBuilder<Builder> {

    private Representation representationGet;

    private Representation representationPost;

    public Builder(@NonNull Resource identifier, @NonNull String pathPattern) {
      super(identifier, pathPattern);
    }

    public Builder representationGet(Representation representation) {
      this.representationGet = representation;
      return this;
    }

    public Builder representationPost(Representation representation) {
      this.representationPost = representation;
      return this;
    }

    public DirectEndPoint build() {
      return new DirectEndPoint(this);
    }

  }
}
