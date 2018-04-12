package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndpoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndpoint;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.springframework.stereotype.Service;

@Service
public class RepresentationRequestHandlerFactory {

  private final EndpointRequestParameterMapper endpointRequestParameterMapper;

  private final RepresentationResourceProvider representationResourceProvider;

  public RepresentationRequestHandlerFactory(
      @NonNull EndpointRequestParameterMapper endpointRequestParameterMapper,
      @NonNull RepresentationResourceProvider representationResourceProvider) {
    this.endpointRequestParameterMapper = endpointRequestParameterMapper;
    this.representationResourceProvider = representationResourceProvider;
  }

  public RequestHandler<DirectEndpoint> newRepresentationRequestHandler(
      @NonNull DirectEndpoint endpoint) {
    return new DirectEndpointRequestHandler(endpoint, endpointRequestParameterMapper,
        representationResourceProvider);
  }

  public RequestHandler<DynamicEndpoint> newRepresentationRequestHandler(
      @NonNull DynamicEndpoint endpoint) {
    return new DynamicEndpointRequestHandler(endpoint, endpointRequestParameterMapper,
        representationResourceProvider);
  }

}
