package org.dotwebstack.framework.service.openapi.handler;

import static java.lang.String.format;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.graphQlErrorException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notAcceptableException;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.addEvaluatedDwsParameters;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.getParameterNamesOfType;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateParameterExistence;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateRequestBodyNonexistent;
import static org.dotwebstack.framework.service.openapi.helper.GraphQlFormatHelper.formatQuery;
import static org.dotwebstack.framework.service.openapi.helper.GraphQlValueHelper.getStringValue;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPAND_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.RequestBodyResolver.resolveRequestBody;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createNewDataStack;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createNewResponseWriteContext;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.dotwebstack.framework.core.query.GraphQlArgument;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.dotwebstack.framework.service.openapi.exception.GraphQlErrorException;
import org.dotwebstack.framework.service.openapi.exception.NoResultFoundException;
import org.dotwebstack.framework.service.openapi.exception.NotAcceptableException;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper;
import org.dotwebstack.framework.service.openapi.helper.SchemaResolver;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.JsonResponseMapper;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapperException;
import org.dotwebstack.framework.service.openapi.param.ParamHandler;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseHeader;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class CoreRequestHandler implements HandlerFunction<ServerResponse> {

  private static final String DEFAULT_ACCEPT_HEADER_VALUE = "*/*";

  private static final String ARGUMENT_PREFIX = "args.";

  private static final String ENVIRONMENT_PREFIX = "env.";

  private static final String MDC_REQUEST_ID = "requestId";

  private OpenAPI openApi;

  private final ResponseSchemaContext responseSchemaContext;

  private final ResponseContextValidator responseContextValidator;

  private final GraphQL graphQL;

  private final List<ResponseMapper> responseMappers;

  private final JsonResponseMapper jsonResponseMapper;

  private final ParamHandlerRouter paramHandlerRouter;

  private final RequestBodyHandlerRouter requestBodyHandlerRouter;

  private final String pathName;

  private final JexlHelper jexlHelper;

  private EnvironmentProperties properties;

  public CoreRequestHandler(OpenAPI openApi, String pathName, ResponseSchemaContext responseSchemaContext,
      ResponseContextValidator responseContextValidator, GraphQL graphQL, List<ResponseMapper> responseMappers,
      JsonResponseMapper jsonResponseMapper, ParamHandlerRouter paramHandlerRouter,
      RequestBodyHandlerRouter requestBodyHandlerRouter, JexlHelper jexlHelper, EnvironmentProperties properties) {
    this.openApi = openApi;
    this.pathName = pathName;
    this.responseSchemaContext = responseSchemaContext;
    this.graphQL = graphQL;
    this.responseMappers = responseMappers;
    this.jsonResponseMapper = jsonResponseMapper;
    this.paramHandlerRouter = paramHandlerRouter;
    this.responseContextValidator = responseContextValidator;
    this.requestBodyHandlerRouter = requestBodyHandlerRouter;
    this.jexlHelper = jexlHelper;
    this.properties = properties;
  }

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {

    return Mono.fromCallable(() -> getResponse(request))
        .publishOn(Schedulers.elastic())
        .onErrorResume(NotAcceptableException.class, getMonoError(NOT_ACCEPTABLE, "Unsupported media type requested."))
        .onErrorResume(ParameterValidationException.class,
            getMonoError(BAD_REQUEST, "Error while obtaining request parameters."))
        .onErrorResume(ResponseMapperException.class,
            getMonoError(INTERNAL_SERVER_ERROR, "Error while serializing response."))
        .onErrorResume(GraphQlErrorException.class,
            getMonoError(INTERNAL_SERVER_ERROR, "Unexpected error while executing request."))
        .onErrorResume(NoResultFoundException.class, getMonoError(NOT_FOUND, "No results found."))
        .onErrorResume(UnsupportedMediaTypeException.class, getMonoError(UNSUPPORTED_MEDIA_TYPE, "Not supported."))
        .onErrorResume(BadRequestException.class, getMonoError(BAD_REQUEST, "Error while processing the request."))
        .onErrorResume(InvalidConfigurationException.class,
            getMonoError(BAD_REQUEST, "Error while validating the request."));
  }

  private Function<Exception, Mono<? extends ServerResponse>> getMonoError(HttpStatus status, String reason) {
    return exception -> {
      String message = format("[OpenApi] An Exception occurred [%s] resulting in [%d] reason [%s]",
          exception.getMessage(), status.value(), reason);
      return Mono.error(new ResponseStatusException(status, message));
    };
  }

  public void validateSchema() {
    GraphQlField field = responseSchemaContext.getGraphQlField();
    if (responseSchemaContext.getResponses()
        .stream()
        .noneMatch(responseTemplate -> responseTemplate.isApplicable(200, 299))) {
      throw unsupportedOperationException("No response in the 200 range found.");
    }
    validateParameters(field, responseSchemaContext.getParameters(),
        getRequestBodyProperties(responseSchemaContext.getRequestBodyContext()), pathName);
    RequestBodyContext requestBodyContext = responseSchemaContext.getRequestBodyContext();
    if (Objects.nonNull(requestBodyContext)) {
      RequestBody requestBody = resolveRequestBody(openApi, requestBodyContext.getRequestBodySchema());
      this.requestBodyHandlerRouter.getRequestBodyHandler(requestBody)
          .validate(field, requestBody, pathName);
    }
    responseSchemaContext.getResponses()
        .stream()
        .filter(responseTemplate -> responseTemplate.isApplicable(200, 299))
        .forEach(response -> responseContextValidator.validate(response.getResponseObject(), field));
  }

  @SuppressWarnings("rawtypes")
  Map<String, Schema> getRequestBodyProperties(RequestBodyContext requestBodyContext) {
    if (Objects.nonNull(requestBodyContext) && Objects.nonNull(requestBodyContext.getRequestBodySchema())) {
      io.swagger.v3.oas.models.media.MediaType mediaType = requestBodyContext.getRequestBodySchema()
          .getContent()
          .get(MediaType.APPLICATION_JSON.toString());
      Schema<?> schema = SchemaResolver.resolveSchema(openApi, mediaType.getSchema(), mediaType.getSchema()
          .get$ref());
      return schema.getProperties();
    } else {
      return Collections.emptyMap();
    }
  }

  Map<String, String> createResponseHeaders(ResponseTemplate responseTemplate, Map<String, Object> inputParams) {
    JexlContext jexlContext = getBaseJexlContext();

    this.responseSchemaContext.getGraphQlField()
        .getArguments()
        .stream()
        .filter(argument -> Objects.nonNull(argument.getDefaultValue()))
        .forEach(argument -> jexlContext.set(ARGUMENT_PREFIX + argument.getName(),
            getStringValue(argument.getDefaultValue())));

    inputParams.forEach((key, value) -> jexlContext.set(ARGUMENT_PREFIX + key, value.toString()));

    Map<String, ResponseHeader> responseHeaders = responseTemplate.getResponseHeaders();

    return getJexlResults(jexlContext, responseHeaders);
  }

  private JexlContext getBaseJexlContext() {
    JexlContext jexlContext = new MapContext();

    this.properties.getAllProperties()
        .forEach((key, value) -> jexlContext.set(ENVIRONMENT_PREFIX + key, value));
    return jexlContext;
  }

  private Map<String, String> getJexlResults(JexlContext jexlContext, Map<String, ResponseHeader> responseHeaders) {
    return responseHeaders.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey,
            entry -> evaluateJexlExpression(jexlContext, entry.getKey(), responseHeaders)));
  }

  private String evaluateJexlExpression(JexlContext jexlContext, String key, Map<String, ResponseHeader> headers) {
    ResponseHeader header = headers.get(key);
    String jexlExpression = header.getJexlExpression();
    try {
      return this.jexlHelper.evaluateScript(jexlExpression, jexlContext, String.class)
          .orElseThrow(() -> invalidConfigurationException(
              "Jexl expression '{}' for parameter '{}' did not return any value", jexlExpression, key));
    } catch (JexlException e) {
      if (e.getMessage()
          .contains("undefined variable") && Objects.nonNull(header.getDefaultValue())) {
        return header.getDefaultValue();
      }
      throw e;
    }
  }

  @SuppressWarnings("rawtypes")
  private void validateParameters(GraphQlField field, List<Parameter> parameters,
      Map<String, Schema> requestBodyProperties, String pathName) {
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
        .forEach(
            argument -> verifyRequiredWithoutDefaultArgument(argument, parameters, pathName, requestBodyProperties));
  }

  ServerResponse getResponse(ServerRequest request) throws GraphQlErrorException, BadRequestException {
    MDC.put(MDC_REQUEST_ID, UUID.randomUUID()
        .toString());
    Map<String, Object> inputParams = resolveParameters(request);

    HttpStatus httpStatus = getHttpStatus();

    if (httpStatus.is3xxRedirection()) {
      URI location = getLocationHeaderUri(inputParams);

      return ServerResponse.status(httpStatus)
          .location(location)
          .build()
          .block();
    }

    String query = buildQueryString(inputParams);

    if (LOG.isDebugEnabled()) {
      logInputRequest(request);
      LOG.debug("GraphQL query is:\n\n{}\n", formatQuery(query));
    }

    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .variables(inputParams)
        .build();

    ExecutionResult result = graphQL.execute(executionInput);
    if (result.getErrors()
        .isEmpty()) {
      Object data = ((Map) result.getData()).values()
          .iterator()
          .next();

      List<MediaType> acceptHeaders = request.headers()
          .accept();
      ResponseTemplate template = getResponseTemplate(acceptHeaders);

      URI uri = request.uri();

      ResponseWriteContext responseWriteContext = createNewResponseWriteContext(template.getResponseObject(), data,
          inputParams, createNewDataStack(new ArrayDeque<>(), data, inputParams), uri);

      String body;
      if (responseWriteContext.hasSchema()) {
        body = jsonResponseMapper.toResponse(responseWriteContext);

      } else {
        body = getResponseMapper(template.getMediaType(), data.getClass()).toResponse(responseWriteContext);
      }

      Map<String, String> responseHeaders = createResponseHeaders(template, resolveUrlAndHeaderParameters(request));

      ServerResponse.BodyBuilder bodyBuilder = ServerResponse.ok()
          .contentType(template.getMediaType());
      responseHeaders.forEach(bodyBuilder::header);

      return bodyBuilder.body(fromPublisher(Mono.just(body), String.class))
          .block();
    }

    throw graphQlErrorException("GraphQL query returned errors: {}", result.getErrors());
  }

  private HttpStatus getHttpStatus() {
    return responseSchemaContext.getResponses()
        .stream()
        .map(ResponseTemplate::getResponseCode)
        .map(HttpStatus::valueOf)
        .filter(httpStatus1 -> httpStatus1.is2xxSuccessful() || httpStatus1.is3xxRedirection())
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException("No response within range 2xx 3xx configured."));
  }

  private URI getLocationHeaderUri(Map<String, Object> inputParams) {
    JexlContext jexlContext = getBaseJexlContext();
    inputParams.forEach((key, value) -> jexlContext.set(ARGUMENT_PREFIX + key, value.toString()));
    Map<String, ResponseHeader> responseHeaders = responseSchemaContext.getResponses()
        .stream()
        .map(ResponseTemplate::getResponseHeaders)
        .map(Map::entrySet)
        .flatMap(Collection::stream)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    String location = getJexlResults(jexlContext, responseHeaders).get("Location");
    return URI.create(location);
  }

  private ResponseMapper getResponseMapper(MediaType mediaType, Class<?> dataObjectType) {
    return responseMappers.stream()
        .filter(rm -> rm.supportsOutputMimeType(mediaType))
        .filter(rm -> rm.supportsInputObjectClass(dataObjectType))
        .reduce((element, otherElement) -> {
          throw mappingException(
              "Duplicate response mapper found for input data object type '{}' and output media type '{}'.",
              dataObjectType, mediaType);
        })
        .orElseThrow(() -> mappingException(
            "No response mapper found for input data object type '{}' and output media type '{}'.", dataObjectType,
            mediaType));
  }

  ResponseTemplate getResponseTemplate(List<MediaType> acceptHeaders) {
    List<ResponseTemplate> responseTemplates = responseSchemaContext.getResponses();

    List<MediaType> supportedMediaTypes = responseTemplates.stream()
        .filter(response -> response.isApplicable(200, 299))
        .map(ResponseTemplate::getMediaType)
        .collect(Collectors.toList());

    CoreRequestHelper.validateResponseMediaTypesAreConfigured(supportedMediaTypes);

    MediaType responseContentType =
        isAcceptHeaderProvided(acceptHeaders) ? getResponseContentType(acceptHeaders, supportedMediaTypes)
            : getDefaultResponseType(responseTemplates, supportedMediaTypes);

    return responseTemplates.stream()
        .filter(response -> response.isApplicable(200, 299))
        .filter(response -> response.getMediaType()
            .equals(responseContentType))
        .findFirst()
        .orElseThrow(() -> unsupportedOperationException("No response found within the 200 range."));
  }

  Map<String, Object> resolveUrlAndHeaderParameters(ServerRequest request) {
    Map<String, Object> result = new HashMap<>();
    if (Objects.nonNull(this.responseSchemaContext.getParameters())) {

      validateParameterExistence("query", getParameterNamesOfType(this.responseSchemaContext.getParameters(), "query"),
          request.queryParams()
              .keySet());
      validateParameterExistence("path", getParameterNamesOfType(this.responseSchemaContext.getParameters(), "path"),
          request.pathVariables()
              .keySet());

      for (Parameter parameter : this.responseSchemaContext.getParameters()) {
        ParamHandler handler = paramHandlerRouter.getParamHandler(parameter);
        handler.getValue(request, parameter, responseSchemaContext)
            .ifPresent(value -> result.put(handler.getParameterName(parameter), value));
      }
    }
    return result;
  }

  private void logInputRequest(ServerRequest request) {
    LOG.debug("Request received at: {}", request);

    Map<Object, Object> paramMap = new LinkedHashMap<>();
    paramMap.putAll(request.queryParams());
    paramMap.putAll(request.headers()
        .asHttpHeaders());
    paramMap.putAll(request.pathVariables());
    LOG.debug("Request contains following parameters: {}", paramMap.entrySet()
        .stream()
        .map(entry -> entry.getKey() + " -> " + entry.getValue())
        .collect(Collectors.toList()));

    Mono<String> mono = request.bodyToMono(String.class);
    mono.doOnSuccess(value -> {
      if (Objects.nonNull(value)) {
        LOG.debug("Request contains the following body: {}", value);
      }
    });
  }

  Map<String, Object> resolveParameters(ServerRequest request) throws BadRequestException {
    Map<String, Object> result = resolveUrlAndHeaderParameters(request);
    RequestBodyContext requestBodyContext = this.responseSchemaContext.getRequestBodyContext();

    if (Objects.nonNull(requestBodyContext)) {
      RequestBody requestBody = resolveRequestBody(openApi, requestBodyContext.getRequestBodySchema());

      this.requestBodyHandlerRouter.getRequestBodyHandler(requestBody)
          .getValues(request, requestBodyContext, requestBody, result)
          .forEach(result::put);
    } else {
      validateRequestBodyNonexistent(request);
    }

    return addEvaluatedDwsParameters(result, responseSchemaContext.getDwsParameters(), request, jexlHelper);
  }

  @SuppressWarnings("rawtypes")
  private void verifyRequiredWithoutDefaultArgument(GraphQlArgument argument, List<Parameter> parameters,
      String pathName, Map<String, Schema> requestBodyProperties) {
    if (argument.isRequired() && Objects.isNull(argument.getDefaultValue()) && (parameters.stream()
        .noneMatch(parameter -> Boolean.TRUE.equals(parameter.getRequired()) && parameter.getName()
            .equals(argument.getName())))
        && !requestBodyProperties.containsKey(argument.getName())) {
      throw invalidConfigurationException(
          "No required OAS parameter found for required and no-default GraphQL argument '{}' in path '{}'",
          argument.getName(), pathName);
    }
    if (argument.isRequired()) {
      argument.getChildren()
          .forEach(child -> verifyRequiredWithoutDefaultArgument(child, parameters, pathName, requestBodyProperties));
    }
  }

  private String buildQueryString(Map<String, Object> inputParams) {
    return new GraphQlQueryBuilder().toQuery(this.responseSchemaContext, inputParams);
  }

  private MediaType getDefaultResponseType(List<ResponseTemplate> responseTemplates,
      List<MediaType> supportedMediaTypes) {
    return responseTemplates.stream()
        .filter(ResponseTemplate::isDefault)
        .findFirst()
        .map(ResponseTemplate::getMediaType)
        .orElse(supportedMediaTypes.get(0));
  }

  private MediaType getResponseContentType(List<MediaType> requestedMediaTypes, List<MediaType> supportedMediaTypes) {
    MediaType.sortByQualityValue(requestedMediaTypes);

    for (MediaType requestedMediaType : requestedMediaTypes) {
      for (MediaType supportedMediaType : supportedMediaTypes) {
        if (requestedMediaType.isCompatibleWith(supportedMediaType)) {
          return supportedMediaType;
        }
      }
    }

    throw notAcceptableException("Unsupported media type provided");
  }

  private boolean isAcceptHeaderProvided(List<MediaType> acceptHeaders) {
    if (!acceptHeaders.isEmpty()) {
      return !(acceptHeaders.size() == 1 && acceptHeaders.get(0)
          .toString()
          .equals(DEFAULT_ACCEPT_HEADER_VALUE));
    }
    return false;
  }

}
