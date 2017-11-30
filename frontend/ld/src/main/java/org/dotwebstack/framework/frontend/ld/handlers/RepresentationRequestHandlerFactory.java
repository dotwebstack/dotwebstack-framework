package org.dotwebstack.framework.frontend.ld.handlers;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.parameter.QueryParameterMapper;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.springframework.stereotype.Service;

@Service
public class RepresentationRequestHandlerFactory {

  private final QueryParameterMapper queryParameterMapper;

  public RepresentationRequestHandlerFactory(@NonNull QueryParameterMapper queryParameterMapper) {
    this.queryParameterMapper = queryParameterMapper;
  }

  public RepresentationRequestHandler newRepresentationRequestHandler(
      @NonNull Representation representation) {
    return new RepresentationRequestHandler(representation, queryParameterMapper);
  }

}
