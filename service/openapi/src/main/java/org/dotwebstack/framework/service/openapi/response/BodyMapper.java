package org.dotwebstack.framework.service.openapi.response;

import graphql.ExecutionResult;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import reactor.core.publisher.Mono;

public interface BodyMapper {

  Mono<Object> map(OperationRequest operationRequest, ExecutionResult executionResult);

  boolean supports(String mediaTypeKey, OperationContext operationContext);
}
