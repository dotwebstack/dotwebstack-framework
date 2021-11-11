package org.dotwebstack.framework.service.openapi.response;

import graphql.ExecutionResult;
import java.util.Map;
import java.util.regex.Pattern;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JsonBodyMapper implements BodyMapper {

  private static final Pattern MEDIA_TYPE_PATTERN = Pattern.compile("^application/([a-z]+\\+)json$");

  @Override
  public Mono<Object> map(OperationRequest operationRequest, ExecutionResult executionResult) {
    return Mono.just(Map.of());
  }

  @Override
  public boolean supports(String mediaTypeKey, OperationContext operationContext) {
    var schema = operationContext.getSuccessResponse()
        .getContent()
        .get(mediaTypeKey)
        .getSchema();

    return schema != null && MEDIA_TYPE_PATTERN.matcher(mediaTypeKey)
        .matches();
  }
}
