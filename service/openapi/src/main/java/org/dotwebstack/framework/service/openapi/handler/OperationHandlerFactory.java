package org.dotwebstack.framework.service.openapi.handler;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notAcceptableException;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.swagger.v3.oas.models.Operation;
import java.util.Map;
import java.util.function.Function;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.dotwebstack.framework.service.openapi.query.QueryFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class OperationHandlerFactory {

  private final GraphQL graphQL;

  private final QueryFactory queryFactory;

  public OperationHandlerFactory(GraphQL graphQL, QueryFactory queryFactory) {
    this.graphQL = graphQL;
    this.queryFactory = queryFactory;
  }

  public HandlerFunction<ServerResponse> create(Operation operation) {
    var operationContext = OperationContext.builder()
        .operation(operation)
        .successResponse(MapperUtils.getSuccessResponse(operation))
        .build();

    var requestInputHandler = createOperationRequestHandler(operationContext);

    return serverRequest -> Mono.just(serverRequest)
        .flatMap(requestInputHandler)
        .map(queryFactory::create)
        .flatMap(this::execute)
        .flatMap(this::mapResponse);
  }

  private Function<ServerRequest, Mono<OperationRequest>> createOperationRequestHandler(
      OperationContext operationContext) {
    var contentNegotiator = createContentNegotiator(operationContext);

    return serverRequest -> resolveParameters(serverRequest).map(parameters -> OperationRequest.builder()
        .context(operationContext)
        .parameters(parameters)
        .preferredMediaType(contentNegotiator.negotiate(serverRequest))
        .build());
  }

  private Mono<Map<String, Object>> resolveParameters(ServerRequest serverRequest) {
    return Mono.just(Map.of());
  }

  private Mono<ExecutionResult> execute(ExecutionInput executionInput) {
    return Mono.fromFuture(graphQL.executeAsync(executionInput));
  }

  private Mono<ServerResponse> mapResponse(ExecutionResult executionResult) {
    return ServerResponse.ok()
        .build();
  }

  private ContentNegotiator createContentNegotiator(OperationContext operationContext) {
    // TODO Real negotiation
    return serverRequest -> operationContext.getSuccessResponse()
        .getContent()
        .keySet()
        .stream()
        .findFirst()
        .orElseThrow(() -> notAcceptableException("None of the acceptable media type is supported."));
  }
}
