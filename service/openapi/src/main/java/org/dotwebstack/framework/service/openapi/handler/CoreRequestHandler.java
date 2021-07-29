package org.dotwebstack.framework.service.openapi.handler;

import static java.util.Collections.emptyList;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.dotwebstack.framework.core.jexl.JexlHelper.getJexlContext;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.graphQlErrorException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.mappingException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.noContentException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notAcceptableException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notFoundException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.parameterValidationException;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.addEvaluatedDwsParameters;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.getParameterNamesOfType;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateParameterExistence;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateRequestBodyNonexistent;
import static org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper.validateRequiredField;
import static org.dotwebstack.framework.service.openapi.helper.GraphQlFormatHelper.formatQuery;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPAND_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_FALLBACK_VALUE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_VALUE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.RequestBodyResolver.resolveRequestBody;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createNewDataStack;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createNewResponseWriteContext;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.execution.InputMapDefinesTooManyFieldsException;
import graphql.execution.NonNullableValueCoercedAsNullException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlContext;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.graphql.GraphQlService;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.dotwebstack.framework.core.query.GraphQlArgument;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.templating.TemplateResponseMapper;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.dotwebstack.framework.service.openapi.exception.GraphQlErrorException;
import org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper;
import org.dotwebstack.framework.service.openapi.helper.SchemaResolver;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.JsonResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.dotwebstack.framework.service.openapi.response.ResponseHeader;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.zalando.problem.ThrowableProblem;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class CoreRequestHandler implements HandlerFunction<ServerResponse> {

  private static final String DEFAULT_ACCEPT_HEADER_VALUE = "*/*";

  private static final String MDC_REQUEST_ID = "requestId";

  private static final String REQUEST_URI = "request_uri";

  private final OpenAPI openApi;

  private final ResponseSchemaContext responseSchemaContext;

  private final GraphQlService graphQL;

  private final List<ResponseMapper> responseMappers;

  private final JsonResponseMapper jsonResponseMapper;

  private final TemplateResponseMapper templateResponseMapper;

  private final ParamHandlerRouter paramHandlerRouter;

  private final RequestBodyHandlerRouter requestBodyHandlerRouter;

  private final String pathName;

  private final JexlHelper jexlHelper;

  private final EnvironmentProperties properties;

  public CoreRequestHandler(OpenAPI openApi, String pathName, ResponseSchemaContext responseSchemaContext,
     GraphQlService graphQL, List<ResponseMapper> responseMappers,
      JsonResponseMapper jsonResponseMapper, TemplateResponseMapper templateResponseMapper,
      ParamHandlerRouter paramHandlerRouter, RequestBodyHandlerRouter requestBodyHandlerRouter, JexlHelper jexlHelper,
      EnvironmentProperties properties) {
    this.openApi = openApi;
    this.pathName = pathName;
    this.responseSchemaContext = responseSchemaContext;
    this.graphQL = graphQL;
    this.responseMappers = responseMappers;
    this.jsonResponseMapper = jsonResponseMapper;
    this.templateResponseMapper = templateResponseMapper;
    this.paramHandlerRouter = paramHandlerRouter;
    this.requestBodyHandlerRouter = requestBodyHandlerRouter;
    this.jexlHelper = jexlHelper;
    this.properties = properties;
  }

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    var requestId = UUID.randomUUID()
        .toString();
    return Mono.fromCallable(() -> getResponse(request, requestId))
        .publishOn(Schedulers.boundedElastic());
  }

  public void validateSchema() {
    if (responseSchemaContext.getResponses()
        .stream()
        .noneMatch(responseTemplate -> responseTemplate.isApplicable(200, 299))) {
      throw unsupportedOperationException("No response in the 200 range found.");
    }
  }

  @SuppressWarnings("rawtypes")
  Map<String, Schema> getRequestBodyProperties(RequestBodyContext requestBodyContext) {
    if (Objects.nonNull(requestBodyContext) && Objects.nonNull(requestBodyContext.getRequestBodySchema())) {
      var mediaType = requestBodyContext.getRequestBodySchema()
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
    var jexlContext =
        getJexlContext(properties.getAllProperties(), inputParams, null);

    Map<String, ResponseHeader> responseHeaders = responseTemplate.getResponseHeaders();

    return getJexlResults(jexlContext, responseHeaders);
  }

  private Map<String, String> getJexlResults(JexlContext jexlContext, Map<String, ResponseHeader> responseHeaders) {
    return responseHeaders.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
          String result = evaluateJexlExpression(jexlContext, entry.getKey(), responseHeaders).orElse(entry.getValue()
              .getDefaultValue());
          if (Objects.isNull(result)) {
            throw invalidConfigurationException("Jexl expression for header '{}' did not return any value",
                entry.getKey());
          }
          return result;
        }));
  }

  private Optional<String> evaluateJexlExpression(JexlContext jexlContext, String key,
      Map<String, ResponseHeader> headers) {
    ResponseHeader header = headers.get(key);
    Map<String, String> dwsExprMap = header.getDwsExpressionMap();
    return this.jexlHelper.evaluateScriptWithFallback(dwsExprMap.get(X_DWS_EXPR_VALUE),
        dwsExprMap.get(X_DWS_EXPR_FALLBACK_VALUE), jexlContext, String.class);
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

  @SuppressWarnings("unchecked")
  ServerResponse getResponse(ServerRequest request, String requestId)
      throws GraphQlErrorException, BadRequestException {
    MDC.put(MDC_REQUEST_ID, requestId);
    Map<String, Object> inputParams = resolveParameters(request);

    ExecutionResult result = buildQueryString(inputParams).map(query -> {
      if (LOG.isDebugEnabled()) {
        logInputRequest(request);
        LOG.debug("GraphQL query is:\n\n{}\n", formatQuery(query));
      }

      var executionInput = ExecutionInput.newExecutionInput()
          .query(query)
          .variables(inputParams)
          .build();


      return graphQL.execute(executionInput);
    })
        .orElse(new ExecutionResultImpl(new HashMap<String, Object>(), emptyList()));

    if (result.getErrors()
        .isEmpty()) {

      if (isQueryExecuted(result.getData()) && !objectExists(result.getData())) {
        throw notFoundException("Did not find data for your response.");
      }

      var httpStatus = getHttpStatus();
      if (httpStatus.is3xxRedirection()) {
        var location = getLocationHeaderUri(inputParams, result.getData());

        return ServerResponse.status(httpStatus)
            .location(location)
            .build()
            .block();
      }

      Object queryResultData = ((Map) result.getData()).values()
          .iterator()
          .next();

      List<MediaType> acceptHeaders = request.headers()
          .accept();
      var template = getResponseTemplate(acceptHeaders);

      String body;

      if (template.usesTemplating()) {
        body = templateResponseMapper.toResponse(template.getTemplateName(), inputParams, queryResultData,
            properties.getAllProperties());
      } else {
        body = getResponseMapperBody(request, inputParams, queryResultData, template);
      }

      if (Objects.isNull(body)) {
        throw noContentException("No content found.");
      }

      Map<String, String> responseHeaders = createResponseHeaders(template, resolveUrlAndHeaderParameters(request));

      var bodyBuilder = ServerResponse.ok()
          .contentType(template.getMediaType());
      responseHeaders.forEach(bodyBuilder::header);

      return bodyBuilder.body(fromPublisher(Mono.just(body), String.class))
          .block();
    }

    if (hasDirectiveValidationException(result)) {
      throw parameterValidationException("Validation of request parameters failed");
    }

    throw unwrapExceptionWhileNeeded(result);
  }

  private GraphQlErrorException unwrapExceptionWhileNeeded(ExecutionResult result) throws GraphQlErrorException {
    var graphQlError = result.getErrors()
        .get(0);

    Optional<ThrowableProblem> throwableProblem = Optional.of(graphQlError)
        .filter(ExceptionWhileDataFetching.class::isInstance)
        .map(ExceptionWhileDataFetching.class::cast)
        .map(ExceptionWhileDataFetching::getException)
        .filter(ThrowableProblem.class::isInstance)
        .map(ThrowableProblem.class::cast);

    if (throwableProblem.isPresent()) {
      throw throwableProblem.get();
    }

    if (graphQlError instanceof InputMapDefinesTooManyFieldsException) {
      throw graphQlErrorException("Too many request fields", graphQlError);
    }

    if (graphQlError instanceof NonNullableValueCoercedAsNullException) {
      throw graphQlErrorException("Missing request fields", graphQlError);
    }

    return graphQlErrorException("GraphQL query returned errors: {}", result.getErrors());
  }

  private boolean hasDirectiveValidationException(ExecutionResult result) {
    return result.getErrors()
        .stream()
        .anyMatch(e -> e instanceof ExceptionWhileDataFetching
            && ((ExceptionWhileDataFetching) e).getException() instanceof DirectiveValidationException);
  }

  private String getResponseMapperBody(ServerRequest request, Map<String, Object> inputParams, Object data,
      ResponseTemplate template) {
    var uri = request.uri();

    if (Objects.nonNull(template.getResponseObject())) {
      var responseWriteContext =
          createNewResponseWriteContext(template.getResponseObject(), data,
              inputParams, createNewDataStack(new ArrayDeque<>(), data, inputParams), uri);

      return jsonResponseMapper.toResponse(responseWriteContext);

    } else {
      return getResponseMapper(template.getMediaType(), data.getClass()).toResponse(data);
    }
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

  private boolean isQueryExecuted(Map<String, Object> resultData) {
    return !resultData.isEmpty();
  }

  private URI getLocationHeaderUri(Map<String, Object> inputParams, Map<String, Object> resultData) {
    var jexlContext = getJexlContext(properties.getAllProperties(), inputParams, resultData);
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
      result.put(REQUEST_URI, request.uri()
          .toString());

      validateParameterExistence("query", getParameterNamesOfType(this.responseSchemaContext.getParameters(), "query"),
          request.queryParams()
              .keySet());
      validateParameterExistence("path", getParameterNamesOfType(this.responseSchemaContext.getParameters(), "path"),
          request.pathVariables()
              .keySet());

      for (Parameter parameter : this.responseSchemaContext.getParameters()) {
        var handler = paramHandlerRouter.getParamHandler(parameter);
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
    var requestBodyContext = this.responseSchemaContext.getRequestBodyContext();

    if (Objects.nonNull(requestBodyContext)) {
      var requestBody = resolveRequestBody(openApi, requestBodyContext.getRequestBodySchema());

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

  protected Optional<String> buildQueryString(Map<String, Object> inputParams) {
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

  private boolean objectExists(Map<String, Object> resultData) {
    return resultData.get(responseSchemaContext.getGraphQlBinding().getQueryName()) != null;
  }

}
