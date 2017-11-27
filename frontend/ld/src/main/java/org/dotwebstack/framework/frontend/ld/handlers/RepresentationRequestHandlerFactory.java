package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.springframework.stereotype.Service;

@Service
public class RepresentationRequestHandlerFactory {

  private final RepresentationRequestParameterMapper representationRequestParameterMapper;

  public RepresentationRequestHandlerFactory(
      @NonNull RepresentationRequestParameterMapper representationRequestParameterMapper) {
    this.representationRequestParameterMapper = representationRequestParameterMapper;
  }

  public RepresentationRequestHandler newRepresentationRequestHandler(
      @NonNull Representation representation) {
    return new RepresentationRequestHandler(representation, representationRequestParameterMapper);
  }

}
