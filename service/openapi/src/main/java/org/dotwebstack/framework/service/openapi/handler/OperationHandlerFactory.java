package org.dotwebstack.framework.service.openapi.handler;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.internalServerErrorException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notAcceptableException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notFoundException;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.defaultMediaTypeFirst;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.getHandleableResponseEntry;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.service.openapi.param.ParameterResolverFactory;
import org.dotwebstack.framework.service.openapi.query.QueryMapper;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;
import org.dotwebstack.framework.service.openapi.response.BodyMapper;
import org.dotwebstack.framework.service.openapi.response.header.ResponseHeaderResolver;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.zalando.problem.ThrowableProblem;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OperationHandlerFactory {

  private final GraphQL graphQL;

  private final QueryMapper queryMapper;

  private final Collection<BodyMapper> bodyMappers;

  private final ParameterResolverFactory parameterResolverFactory;

  private final ResponseHeaderResolver responseHeaderResolver;

  public OperationHandlerFactory(GraphQL graphQL, QueryMapper queryMapper, Collection<BodyMapper> bodyMappers,
      ParameterResolverFactory parameterResolverFactory, ResponseHeaderResolver responseHeaderResolver) {
    this.graphQL = graphQL;
    this.queryMapper = queryMapper;
    this.bodyMappers = bodyMappers;
    this.parameterResolverFactory = parameterResolverFactory;
    this.responseHeaderResolver = responseHeaderResolver;
  }

  public HandlerFunction<ServerResponse> create(Operation operation) {
    var operationContext = OperationContext.builder()
        .operation(operation)
        .responseEntry(getHandleableResponseEntry(operation))
        .queryProperties(QueryProperties.fromOperation(operation))
        .build();

    var requestHandler = createRequestHandler(operationContext);
    var responseHandler = createResponseHandler(operationContext);

    if (!operationContext.hasQuery()) {
      return serverRequest -> requestHandler.apply(serverRequest)
          .flatMap(operationRequest -> responseHandler.apply(null, operationRequest));
    }

    return serverRequest -> requestHandler.apply(serverRequest)
        .flatMap(operationRequest -> Mono.just(queryMapper.map(operationRequest))
            .flatMap(this::execute)
            .flatMap(executionResult -> responseHandler.apply(executionResult, operationRequest)));
  }

  private Function<ServerRequest, Mono<OperationRequest>> createRequestHandler(OperationContext operationContext) {
    var parameterResolver = parameterResolverFactory.create(operationContext.getOperation());

    return serverRequest -> parameterResolver.resolveParameters(serverRequest)
        .map(parameters -> buildOperationRequest(operationContext, parameters, serverRequest));
  }

  private OperationRequest buildOperationRequest(OperationContext operationContext, Map<String, Object> parameters,
      ServerRequest serverRequest) {
    var builder = OperationRequest.builder()
        .context(operationContext)
        .parameters(parameters)
        .serverRequest(serverRequest);

    if (operationContext.isResponseWithBody()) {
      var contentNegotiator = createContentNegotiator(operationContext.getResponse());
      builder.preferredMediaType(contentNegotiator.negotiate(serverRequest));
    }

    return builder.build();
  }

  private BiFunction<ExecutionResult, OperationRequest, Mono<ServerResponse>> createResponseHandler(
      OperationContext operationContext) {
    var bodyMapperMap = createBodyMapperMap(operationContext);

    return (executionResult, operationRequest) -> {
      Object queryResult = null;
      if (operationContext.hasQuery()) {
        Map<String, Object> data = executionResult.getData();
        queryResult = data.get(operationContext.getQueryProperties()
            .getField());

        if (queryResult == null) {
          return Mono.error(notFoundException("Did not find data for your response."));
        }
      }

      if (operationContext.isResponseWithBody()) {
        return bodyMapperMap.get(operationRequest.getPreferredMediaType())
            .map(operationRequest, queryResult)
            .flatMap(content -> ServerResponse.status(operationContext.getHttpStatus())
                .contentType(operationRequest.getPreferredMediaType())
                .headers(responseHeaderResolver.resolve(operationRequest, content))
                .body(BodyInserters.fromValue(content)));
      }

      return ServerResponse.status(operationContext.getHttpStatus())
          .headers(responseHeaderResolver.resolve(operationRequest, queryResult))
          .build();
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

    Optional.of(errors.get(0))
        .filter(ExceptionWhileDataFetching.class::isInstance)
        .map(ExceptionWhileDataFetching.class::cast)
        .map(ExceptionWhileDataFetching::getException)
        .filter(ThrowableProblem.class::isInstance)
        .map(ThrowableProblem.class::cast)
        .ifPresent(throwableProblem -> {
          throw throwableProblem;
        });

    return Mono.error(internalServerErrorException());
  }

  private ContentNegotiator createContentNegotiator(ApiResponse bodyResponse) {
    var supportedMediaTypes = bodyResponse.getContent()
        .entrySet()
        .stream()
        .sorted((mediaTypeEntry1, mediaTypeEntry2) -> defaultMediaTypeFirst(mediaTypeEntry1.getValue(),
            mediaTypeEntry2.getValue()))
        .map(Map.Entry::getKey)
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
    if (!operationContext.isResponseWithBody()) {
      return Map.of();
    }

    return operationContext.getResponse()
        .getContent()
        .keySet()
        .stream()
        .collect(Collectors.toMap(MediaType::valueOf,
            mediaType -> findBodyMapper(MediaType.valueOf(mediaType), operationContext)));
  }

  private BodyMapper findBodyMapper(MediaType mediaType, OperationContext operationContext) {
    return bodyMappers.stream()
        .filter(responseMapper -> responseMapper.supports(mediaType, operationContext))
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException("Could not find body mapper for media type {}.", mediaType));
  }
}
