package org.dotwebstack.framework.frontend.openapi.entity;

import io.swagger.models.Response;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;

public interface Entity {

  Response getResponse();

  RequestContext getRequestContext();

}
