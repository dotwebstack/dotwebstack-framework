package org.dotwebstack.framework.service.openapi.handler;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;

public interface ContentNegotiator {

  MediaType negotiate(ServerRequest serverRequest);
}
