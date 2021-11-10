package org.dotwebstack.framework.service.openapi.handler;

import org.springframework.web.reactive.function.server.ServerRequest;

public interface ContentNegotiator {

  String negotiate(ServerRequest serverRequest);
}
