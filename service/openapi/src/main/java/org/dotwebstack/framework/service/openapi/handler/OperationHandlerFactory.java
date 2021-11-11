package org.dotwebstack.framework.service.openapi.handler;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notAcceptableException;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.swagger.v3.oas.models.Operation;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.dotwebstack.framework.service.openapi.param.ParameterResolverFactory;
import org.dotwebstack.framework.service.openapi.query.QueryMapper;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;
import org.dotwebstack.framework.service.openapi.response.BodyMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OperationHandlerFactory {

  private final GraphQL graphQL;

  private final QueryMapper queryMapper;

  private final Collection<BodyMapper> bodyMappers;

  private final ParameterResolverFactory parameterResolverFactory;

  public OperationHandlerFactory(GraphQL graphQL, QueryMapper queryMapper, Collection<BodyMapper> bodyMappers,
      ParameterResolverFactory parameterResolverFactory) {
    this.graphQL = graphQL;
    this.queryMapper = queryMapper;
    this.bodyMappers = bodyMappers;
    this.parameterResolverFactory = parameterResolverFactory;
  }

  public HandlerFunction<ServerResponse> create(Operation operation) {
    var operationContext = OperationContext.builder()
        .operation(operation)
        .queryProperties(QueryProperties.fromOperation(operation))
        .successResponse(MapperUtils.getSuccessResponse(operation))
        .build();

    var mediaTypeContentMappers = createMediaTypeBodyMappers(operationContext);
    var requestInputHandler = createOperationRequestHandler(operationContext);

    return serverRequest -> requestInputHandler.apply(serverRequest)
        .flatMap(operationRequest -> Mono.just(queryMapper.map(operationRequest))
            .flatMap(this::execute)
            .flatMap(executionResult -> mediaTypeContentMappers.get(operationRequest.getPreferredMediaType())
                .map(operationRequest, executionResult)
                .flatMap(content -> ServerResponse.ok()
                    .body(BodyInserters.fromValue(content)))));
  }

  private Function<ServerRequest, Mono<OperationRequest>> createOperationRequestHandler(
      OperationContext operationContext) {
    var contentNegotiator = createContentNegotiator(operationContext);
    var parameterResolver = parameterResolverFactory.create(operationContext.getOperation());

    return serverRequest -> parameterResolver.resolveParameters(serverRequest)
        .map(parameters -> OperationRequest.builder()
            .context(operationContext)
            .parameters(parameters)
            .preferredMediaType(contentNegotiator.negotiate(serverRequest))
            .build());
  }

  private Mono<ExecutionResult> execute(ExecutionInput executionInput) {
    LOG.debug("Executing query:\n{}", executionInput.getQuery());
    LOG.debug("Query variables:\n{}", executionInput.getVariables());

    return Mono.fromFuture(graphQL.executeAsync(executionInput));
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

  private Map<String, BodyMapper> createMediaTypeBodyMappers(OperationContext operationContext) {
    return operationContext.getSuccessResponse()
        .getContent()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> findBodyMapper(entry.getKey(), operationContext)));
  }

  private BodyMapper findBodyMapper(String mediaTypeKey, OperationContext operationContext) {
    return bodyMappers.stream()
        .filter(responseMapper -> responseMapper.supports(mediaTypeKey, operationContext))
        .findFirst()
        .orElseThrow(() -> ExceptionHelper.invalidConfigurationException("Could not find response mapper."));
  }
}
