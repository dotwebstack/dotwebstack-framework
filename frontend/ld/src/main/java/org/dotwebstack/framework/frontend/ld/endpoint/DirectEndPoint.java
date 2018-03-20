package org.dotwebstack.framework.frontend.ld.endpoint;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.service.Service;
import org.eclipse.rdf4j.model.Resource;

public class DirectEndPoint extends AbstractEndPoint {

  private Representation getRepresentation;

  private Representation postRepresentation;

  private List<Service> deleteService;

  private List<Service> postService;

  private List<Service> putService;

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

  public List<Service> getDeleteService() {
    return deleteService;
  }

  public List<Service> getPostService() {
    return postService;
  }

  public List<Service> getPutService() {
    return putService;
  }

  public static class Builder extends EndPointBuilder<Builder> {

    private Representation getRepresentation;

    private Representation postRepresentation;

    private List<Service> deleteService;

    private List<Service> postService;

    private List<Service> putService;

    public Builder(@NonNull Resource identifier, @NonNull String pathPattern) {
      super(identifier, pathPattern);
      this.deleteService = new ArrayList<>();
      this.postService = new ArrayList<>();
      this.putService = new ArrayList<>();
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
      this.deleteService.add(deleteService);
      return this;
    }

    public Builder postService(Service postService) {
      this.postService.add(postService);
      return this;
    }

    public Builder putService(Service putService) {
      this.putService.add(putService);
      return this;
    }

    public DirectEndPoint build() {
      return new DirectEndPoint(this);
    }
  }

}
