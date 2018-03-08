package org.dotwebstack.framework.frontend.ld.handlers;

import javax.xml.ws.Endpoint;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class RepresentationRequestHandlerFactory {

  private final RepresentationRequestParameterMapper representationRequestParameterMapper;

  public RepresentationRequestHandlerFactory(
      @NonNull RepresentationRequestParameterMapper representationRequestParameterMapper) {
    this.representationRequestParameterMapper = representationRequestParameterMapper;
  }

  public RepresentationRequestHandler newRepresentationRequestHandler(@NonNull Endpoint endpoint) {
    return new RepresentationRequestHandler(endpoint, representationRequestParameterMapper);
  }

}
