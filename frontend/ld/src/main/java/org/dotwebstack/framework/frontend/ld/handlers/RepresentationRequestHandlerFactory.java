package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.endpoint.DirectEndPoint;
import org.dotwebstack.framework.frontend.ld.endpoint.DynamicEndPoint;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.springframework.stereotype.Service;

@Service
public class RepresentationRequestHandlerFactory {

  private final EndPointRequestParameterMapper endPointRequestParameterMapper;

  private final RepresentationResourceProvider representationResourceProvider;

  public RepresentationRequestHandlerFactory(
      @NonNull EndPointRequestParameterMapper endPointRequestParameterMapper,
      @NonNull RepresentationResourceProvider representationResourceProvider) {
    this.endPointRequestParameterMapper = endPointRequestParameterMapper;
    this.representationResourceProvider = representationResourceProvider;
  }

  public RequestHandler<DirectEndPoint> newRepresentationRequestHandler(
      @NonNull DirectEndPoint endpoint) {
    return new DirectEndPointRequestHandler(endpoint, endPointRequestParameterMapper,
        representationResourceProvider);
  }

  public RequestHandler<DynamicEndPoint> newRepresentationRequestHandler(
      @NonNull DynamicEndPoint endpoint) {
    return new DynamicEndPointRequestHandler(endpoint, endPointRequestParameterMapper,
        representationResourceProvider);
  }

}
