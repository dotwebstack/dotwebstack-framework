package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.v3.oas.models.responses.ApiResponse;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;

public interface Entity {

  ApiResponse getResponse();

  RequestContext getRequestContext();

}
