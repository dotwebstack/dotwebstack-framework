package org.dotwebstack.framework.frontend.ld.endpoint;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.service.Service;
import org.eclipse.rdf4j.model.Resource;

public class DirectEndPoint extends AbstractEndPoint {

  private Representation getRepresentation;

  private Representation postRepresentation;

  private Service deleteService;

  private Service postService;

  private Service putService;

  public DirectEndPoint(Builder builder) {
    super(builder);
    this.getRepresentation = builder.getRepresentation;
    this.postRepresentation = builder.postRepresentation;
    this.deleteService = builder.deleteService;
    this.postService = builder.postService;
    this.putService = builder.putService;
  }

  public Representation getGetRepresentation() {
    return getRepresentation;
  }

  public Representation getPostRepresentation() {
    return postRepresentation;
  }

  public Service getDeleteService() {
    return deleteService;
  }

  public Service getPostService() {
    return postService;
  }

  public Service getPutService() {
    return putService;
  }

  public static class Builder extends EndPointBuilder<Builder> {

    private Representation getRepresentation;

    private Representation postRepresentation;

    private Service deleteService;

    private Service postService;

    private Service putService;

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

    public Builder deleteService(Service deleteService) {
      this.deleteService = deleteService;
      return this;
    }

    public Builder postService(Service postService) {
      this.postService = postService;
      return this;
    }

    public Builder putService(Service putService) {
      this.putService = putService;
      return this;
    }

    public DirectEndPoint build() {
      return new DirectEndPoint(this);
    }
  }

}
