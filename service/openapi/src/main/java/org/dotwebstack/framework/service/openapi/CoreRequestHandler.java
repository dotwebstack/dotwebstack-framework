package org.dotwebstack.framework.service.openapi;

import static java.lang.String.format;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPAND_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlArgument;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.dotwebstack.framework.service.openapi.exception.GraphQlErrorException;
import org.dotwebstack.framework.service.openapi.exception.NoResultFoundException;
import org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandler;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.param.RequestBodyHandler;
import org.dotwebstack.framework.service.openapi.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class CoreRequestHandler implements HandlerFunction<ServerResponse> {

  private final ResponseContext responseContext;

  private final ResponseContextValidator responseContextValidator;

  private final GraphQL graphQL;

  private final ResponseMapper responseMapper;

  private final ParamHandlerRouter paramHandlerRouter;

  private final RequestBodyHandler requestBodyHandler;

  private String pathName;

  CoreRequestHandler(String pathName, ResponseContext responseContext,
      ResponseContextValidator responseContextValidator, GraphQL graphQL, ResponseMapper responseMapper,
      ParamHandlerRouter paramHandlerRouter, RequestBodyHandler requestBodyHandler) {
    this.pathName = pathName;
    this.responseContext = responseContext;
    this.graphQL = graphQL;
    this.responseMapper = responseMapper;
    this.paramHandlerRouter = paramHandlerRouter;
    this.responseContextValidator = responseContextValidator;
    this.requestBodyHandler = requestBodyHandler;
    validateSchema();
  }

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    Mono<String> bodyPublisher = Mono.fromCallable(() -> getResponse(request))
        .publishOn(Schedulers.elastic())
        .onErrorResume(ParameterValidationException.class,
            e -> getMonoError(format("Error while obtaining " + "request parameters: %s", e.getMessage()),
                HttpStatus.BAD_REQUEST))
        .onErrorResume(JsonProcessingException.class,
            e -> getMonoError("Error while serializing response to JSON" + ".", HttpStatus.INTERNAL_SERVER_ERROR))
        .onErrorResume(GraphQlErrorException.class, e -> getMonoError(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR))
        .onErrorResume(NoResultFoundException.class, e -> getMonoError(null, HttpStatus.NOT_FOUND))
        .onErrorResume(UnsupportedMediaTypeException.class, e -> getMonoError(null, HttpStatus.UNSUPPORTED_MEDIA_TYPE))
        .onErrorResume(BadRequestException.class, e -> getMonoError(null, HttpStatus.BAD_REQUEST));

    ResponseTemplate template = getResponseTemplate();
    return ServerResponse.ok()
        .contentType(MediaType.parseMediaType(template.getMediaType()))
        .body(fromPublisher(bodyPublisher, String.class));
  }

  private void validateSchema() {
    GraphQlField field = responseContext.getGraphQlField();
    if (responseContext.getResponses()
        .stream()
        .noneMatch(responseTemplate -> responseTemplate.isApplicable(200, 299))) {
      throw ExceptionHelper.unsupportedOperationException("No response in the 200 range found.");
    }
    validateParameters(field, responseContext.getParameters(), pathName);
    if (responseContext.getRequestBodyContext() != null) {
      this.requestBodyHandler.validate(field, responseContext.getRequestBodyContext()
          .getRequestBody(), pathName);
    }
    responseContext.getResponses()
        .forEach(response -> {
          responseContextValidator.validate(response.getResponseObject(), field);
        });
  }

  private void validateParameters(GraphQlField field, List<Parameter> parameters, String pathName) {
    if (parameters.stream()
        .filter(parameter -> Objects.nonNull(parameter.getExtensions()) && Objects.nonNull(parameter.getExtensions()
            .get(X_DWS_TYPE)) && X_DWS_EXPAND_TYPE.equals(
                parameter.getExtensions()
                    .get(X_DWS_TYPE)))
        .count() > 1) {
      throw invalidConfigurationException("It is not possible to have more than one expand parameter per Operation");
    }
    parameters.forEach(parameter -> this.paramHandlerRouter.getParamHandler(parameter)
        .validate(field, parameter, pathName));
    field.getArguments()
        .forEach(argument -> verifyRequiredWithoutDefaultArgument(argument, parameters, pathName));
  }

  private void verifyRequiredWithoutDefaultArgument(GraphQlArgument argument, List<Parameter> parameters,
      String pathName) {
    if (argument.isRequired() && !argument.isHasDefault() && parameters.stream()
        .noneMatch(parameter -> Boolean.TRUE.equals(parameter.getRequired()) && parameter.getName()
            .equals(argument.getName()))) {
      throw invalidConfigurationException(
          "No required OAS parameter found for required and no-default GraphQL argument" + " '{}' in path '{}'",
          argument.getName(), pathName);
    }
    if (argument.isRequired()) {
      argument.getChildren()
          .forEach(child -> verifyRequiredWithoutDefaultArgument(child, parameters, pathName));
    }
  }

  protected Mono<String> getMonoError(String message, HttpStatus statusCode) {
    return Mono.error(new ResponseStatusException(statusCode, message));
  }

  @SuppressWarnings("unchecked")
  private String getResponse(ServerRequest request)
      throws NoResultFoundException, JsonProcessingException, GraphQlErrorException, BadRequestException {
    Map<String, Object> inputParams = resolveParameters(request);

    String query = buildQueryString(inputParams);
    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .variables(inputParams)
        .build();

    ExecutionResult result = graphQL.execute(executionInput);
    if (result.getErrors()
        .isEmpty()) {
      ResponseTemplate template = getResponseTemplate();

      return responseMapper.toJson(template.getResponseObject(),
          ((Map<String, Object>) result.getData()).get(this.responseContext.getGraphQlField()
              .getName()));
    }
    throw OpenApiExceptionHelper.graphQlErrorException("GraphQL query returned errors: {}", result.getErrors());
  }

  private ResponseTemplate getResponseTemplate() {
    return responseContext.getResponses()
        .stream()
        .filter(response -> response.isApplicable(200, 299))
        .findFirst()
        .orElseThrow(() -> ExceptionHelper.unsupportedOperationException("No response found within the 200 range."));
  }

  private Map<String, Object> resolveParameters(ServerRequest request) throws BadRequestException {
    Map<String, Object> result = new HashMap<>();
    if (Objects.nonNull(this.responseContext.getParameters())) {
      for (Parameter parameter : this.responseContext.getParameters()) {
        ParamHandler handler = paramHandlerRouter.getParamHandler(parameter);
        handler.getValue(request, parameter)
            .ifPresent(value -> result.put(handler.getParameterName(parameter.getName()), value));
      }
    }
    RequestBodyContext requestBodyContext = this.responseContext.getRequestBodyContext();
    if (Objects.nonNull(requestBodyContext)) {
      this.requestBodyHandler.getValue(request, requestBodyContext)
          .ifPresent(value -> result.put(requestBodyContext.getName(), value));
    }
    return result;
  }

  private String buildQueryString(Map<String, Object> inputParams) {
    return new GraphQlQueryBuilder().toQuery(this.responseContext.getGraphQlField(), inputParams);
  }
}
