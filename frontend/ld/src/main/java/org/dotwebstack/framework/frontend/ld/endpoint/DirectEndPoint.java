package org.dotwebstack.framework.frontend.ld.endpoint;

import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.model.Resource;

public class DirectEndPoint extends EndPoint {

  private Representation representationGet;

  private Representation representationPost;

  // private Service postService;
  // private Service putService;
  // private Service deleteService;

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

  // add getters for services

  public static class Builder extends EndPointBuilder {

    private Representation representationGet;

    private Representation representationPost;

    // add services

    public Builder(Resource identifier, String pathPattern) {
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

    // add builder methods for services
  }
}
