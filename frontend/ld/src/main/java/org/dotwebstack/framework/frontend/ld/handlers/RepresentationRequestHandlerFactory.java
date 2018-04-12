package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpoint;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.springframework.stereotype.Service;

@Service
public class RepresentationRequestHandlerFactory {

  private final EndpointRequestParameterMappers endpointRequestParameterMappers;

  private final RepresentationResourceProvider representationResourceProvider;

  public RepresentationRequestHandlerFactory(
      @NonNull EndpointRequestParameterMappers endpointRequestParameterMappers,
      @NonNull RepresentationResourceProvider representationResourceProvider) {
    this.endpointRequestParameterMappers = endpointRequestParameterMappers;
    this.representationResourceProvider = representationResourceProvider;
  }

  public RequestHandler<DirectEndpoint> newRepresentationRequestHandler(
      @NonNull DirectEndpoint endpoint) {
    return new DirectEndpointRequestHandlers(endpoint, endpointRequestParameterMappers,
        representationResourceProvider);
  }

  public RequestHandler<DynamicEndpoint> newRepresentationRequestHandler(
      @NonNull DynamicEndpoint endpoint) {
    return new DynamicEndpointRequestHandlers(endpoint, endpointRequestParameterMappers,
        representationResourceProvider);
  }

}
