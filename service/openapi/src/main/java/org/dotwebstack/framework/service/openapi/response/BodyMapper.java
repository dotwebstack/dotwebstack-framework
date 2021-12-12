package org.dotwebstack.framework.service.openapi.response;

import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

public interface BodyMapper {

  Mono<Object> map(OperationRequest operationRequest, Object result);

  boolean supports(MediaType mediaType, OperationContext operationContext);
}
