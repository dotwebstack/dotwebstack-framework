package org.dotwebstack.framework.service.openapi.handler;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.internalServerErrorException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notAcceptableException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notFoundException;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import io.swagger.v3.oas.models.Operation;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.dotwebstack.framework.service.openapi.param.ParameterResolverFactory;
import org.dotwebstack.framework.service.openapi.query.QueryMapper;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;
import org.dotwebstack.framework.service.openapi.response.BodyMapper;
import org.dotwebstack.framework.service.openapi.response.header.ResponseHeaderResolverFactory;
import org.springframework.http.MediaType;
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

  private final ResponseHeaderResolverFactory responseHeaderResolverFactory;

  public OperationHandlerFactory(GraphQL graphQL, QueryMapper queryMapper, Collection<BodyMapper> bodyMappers,
      ParameterResolverFactory parameterResolverFactory, ResponseHeaderResolverFactory responseHeaderResolverFactory) {
    this.graphQL = graphQL;
    this.queryMapper = queryMapper;
    this.bodyMappers = bodyMappers;
    this.parameterResolverFactory = parameterResolverFactory;
    this.responseHeaderResolverFactory = responseHeaderResolverFactory;
  }

  public HandlerFunction<ServerResponse> create(Operation operation) {
    var operationContext = OperationContext.builder()
        .operation(operation)
        .queryProperties(QueryProperties.fromOperation(operation))
        .successResponse(MapperUtils.getSuccessResponse(operation))
        .build();

    var requestHandler = createRequestHandler(operationContext);
    var responseHandler = createResponseHandler(operationContext);

    return serverRequest -> requestHandler.apply(serverRequest)
        .flatMap(operationRequest -> Mono.just(queryMapper.map(operationRequest))
            .flatMap(this::execute)
            .flatMap(executionResult -> responseHandler.apply(executionResult, operationRequest)));
  }

  private Function<ServerRequest, Mono<OperationRequest>> createRequestHandler(OperationContext operationContext) {
    var contentNegotiator = createContentNegotiator(operationContext);
    var parameterResolver = parameterResolverFactory.create(operationContext.getOperation());

    return serverRequest -> parameterResolver.resolveParameters(serverRequest)
        .map(parameters -> OperationRequest.builder()
            .context(operationContext)
            .parameters(parameters)
            .preferredMediaType(contentNegotiator.negotiate(serverRequest))
            .build());
  }

  private BiFunction<ExecutionResult, OperationRequest, Mono<ServerResponse>> createResponseHandler(
      OperationContext operationContext) {
    var bodyMapperMap = createBodyMapperMap(operationContext);
    var queryField = operationContext.getQueryProperties()
        .getField();

    return (executionResult, operationRequest) -> {
      Map<String, Object> data = executionResult.getData();

      var responseHeaderResolver = responseHeaderResolverFactory.create(operationRequest);

      return Mono.justOrEmpty(data.get(queryField))
          .flatMap(result -> bodyMapperMap.get(operationRequest.getPreferredMediaType())
              .map(operationRequest, result))
          .flatMap(content -> ServerResponse.ok()
              .contentType(operationRequest.getPreferredMediaType())
              .headers(responseHeaderResolver)
              .body(BodyInserters.fromValue(content)))
          .switchIfEmpty(Mono.error(notFoundException("Did not find data for your response.")));
    };
  }

  private Mono<ExecutionResult> execute(ExecutionInput executionInput) {
    LOG.debug("Executing query:\n{}", executionInput.getQuery());
    LOG.debug("Query variables:\n{}", executionInput.getVariables());

    return Mono.fromFuture(graphQL.executeAsync(executionInput))
        .flatMap(this::handleErrors);
  }

  private Mono<ExecutionResult> handleErrors(ExecutionResult executionResult) {
    var errors = executionResult.getErrors();

    if (errors.isEmpty()) {
      return Mono.just(executionResult);
    }

    LOG.error("GraphQL query returned errors:\n{}", errors.stream()
        .map(GraphQLError::getMessage)
        .map("- "::concat)
        .collect(Collectors.joining("\n")));

    return Mono.error(internalServerErrorException());
  }

  private ContentNegotiator createContentNegotiator(OperationContext operationContext) {
    var supportedMediaTypes = operationContext.getSuccessResponse()
        .getContent()
        .keySet()
        .stream()
        .map(MediaType::valueOf)
        .collect(Collectors.toList());

    return serverRequest -> {
      var acceptableMediaTypes = serverRequest.headers()
          .accept();

      if (acceptableMediaTypes.isEmpty()) {
        return supportedMediaTypes.get(0);
      }

      MediaType.sortByQualityValue(acceptableMediaTypes);

      for (MediaType requestedMediaType : acceptableMediaTypes) {
        for (MediaType supportedMediaType : supportedMediaTypes) {
          if (requestedMediaType.isCompatibleWith(supportedMediaType)) {
            return supportedMediaType;
          }
        }
      }

      throw notAcceptableException("None of the acceptable media type is supported.");
    };
  }

  private Map<MediaType, BodyMapper> createBodyMapperMap(OperationContext operationContext) {
    return operationContext.getSuccessResponse()
        .getContent()
        .entrySet()
        .stream()
        .collect(Collectors.toMap(entry -> MediaType.valueOf(entry.getKey()),
            entry -> findBodyMapper(MediaType.valueOf(entry.getKey()), operationContext)));
  }

  private BodyMapper findBodyMapper(MediaType mediaType, OperationContext operationContext) {
    return bodyMappers.stream()
        .filter(responseMapper -> responseMapper.supports(mediaType, operationContext))
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException("Could not find body mapper."));
  }
}
