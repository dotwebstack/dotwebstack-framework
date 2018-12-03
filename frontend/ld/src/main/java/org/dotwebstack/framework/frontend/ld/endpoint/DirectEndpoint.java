package org.dotwebstack.framework.frontend.ld.endpoint;

import javax.ws.rs.HttpMethod;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.service.Service;
import org.eclipse.rdf4j.model.Resource;

public class DirectEndpoint extends AbstractEndpoint {

  private Representation getRepresentation;

  private Representation postRepresentation;

  private Service deleteService;

  private Service postService;

  private Service putService;

  public DirectEndpoint(Builder builder) {
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

  public Representation getRepresentationFor(String method) {
    switch (method) {
      case HttpMethod.GET:
        return getRepresentation;
      case HttpMethod.POST:
        return postRepresentation;
      default:
        throw new IllegalStateException("Unsupported HTTP method");
    }
  }

  public Service getServiceFor(String method) {
    switch (method) {
      case HttpMethod.POST:
        return postService;
      case HttpMethod.PUT:
        return putService;
      case HttpMethod.DELETE:
        return deleteService;
      default:
        throw new IllegalStateException("Unsupported HTTP method " + method);
    }
  }

  public static class Builder extends EndpointBuilder<Builder> {

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

    public DirectEndpoint build() {
      return new DirectEndpoint(this);
    }
  }

}