package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.endpoint.AbstractEndPoint;
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

  public RepresentationRequestHandler newRepresentationRequestHandler(
      @NonNull AbstractEndPoint endpoint) {
    return new RepresentationRequestHandler(endpoint, endPointRequestParameterMapper,
        representationResourceProvider);
  }

}
