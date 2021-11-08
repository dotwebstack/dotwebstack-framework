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
import static org.dotwebstack.framework.service.openapi.helper.GraphQlFormatHelper.formatQuery;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_FALLBACK_VALUE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR_VALUE;
import static org.dotwebstack.framework.service.openapi.helper.RequestBodyResolver.resolveRequestBody;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createNewDataStack;
import static org.dotwebstack.framework.service.openapi.response.ResponseWriteContextHelper.createNewResponseWriteContext;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQL;
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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlContext;
import org.dataloader.DataLoaderRegistry;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.dotwebstack.framework.core.templating.TemplateResponseMapper;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.dotwebstack.framework.service.openapi.exception.GraphQlErrorException;
import org.dotwebstack.framework.service.openapi.helper.CoreRequestHelper;
import org.dotwebstack.framework.service.openapi.helper.SchemaResolver;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.JsonResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.query.QueryInput;
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

@Slf4j
public class CoreRequestHandler implements HandlerFunction<ServerResponse> {

  private static final String DEFAULT_ACCEPT_HEADER_VALUE = "*/*";

  private static final String MDC_REQUEST_ID = "requestId";

  private static final String REQUEST_URI = "request_uri";

  private final OpenAPI openApi;

  private final HttpMethodOperation httpMethodOperation;

  private final ResponseSchemaContext responseSchemaContext;

  private final GraphQL graphQL;

  private final List<ResponseMapper> responseMappers;

  private final JsonResponseMapper jsonResponseMapper;

  private final TemplateResponseMapper templateResponseMapper;

  private final ParamHandlerRouter paramHandlerRouter;

  private final RequestBodyHandlerRouter requestBodyHandlerRouter;

  private final JexlHelper jexlHelper;

  private final EnvironmentProperties properties;

  private final GraphQlQueryBuilder graphQlQueryBuilder;

  public CoreRequestHandler(OpenAPI openApi, HttpMethodOperation httpMethodOperation,
      ResponseSchemaContext responseSchemaContext, GraphQL graphQL, List<ResponseMapper> responseMappers,
      JsonResponseMapper jsonResponseMapper, TemplateResponseMapper templateResponseMapper,
      ParamHandlerRouter paramHandlerRouter, RequestBodyHandlerRouter requestBodyHandlerRouter, JexlHelper jexlHelper,
      EnvironmentProperties properties, GraphQlQueryBuilder queryBuilder) {
    this.openApi = openApi;
    this.httpMethodOperation = httpMethodOperation;
    this.responseSchemaContext = responseSchemaContext;
    this.graphQL = graphQL;
    this.responseMappers = responseMappers;
    this.jsonResponseMapper = jsonResponseMapper;
    this.templateResponseMapper = templateResponseMapper;
    this.paramHandlerRouter = paramHandlerRouter;
    this.requestBodyHandlerRouter = requestBodyHandlerRouter;
    this.jexlHelper = jexlHelper;
    this.properties = properties;
    this.graphQlQueryBuilder = queryBuilder;
  }

  @Override
  public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
    var requestId = UUID.randomUUID()
        .toString();

    return getResponse(request, requestId);
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
    var jexlContext = getJexlContext(properties.getAllProperties(), inputParams, null);

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

  Mono<ServerResponse> getResponse(ServerRequest request, String requestId) {
    MDC.put(MDC_REQUEST_ID, requestId);

    if (LOG.isDebugEnabled()) {
      logInputRequest(request);
    }

    return resolveParameters(request).flatMap(inputParams -> getQueryInput(inputParams).map(this::executeQuery)
        .orElse(Mono.just(new ExecutionResultImpl(new HashMap<String, Object>(), emptyList())))
        .flatMap(result -> handleResult(result, request, inputParams)));
  }

  private Mono<ExecutionResult> executeQuery(QueryInput input) {
    String query = input.getQuery();

    if (LOG.isDebugEnabled()) {
      LOG.debug("GraphQL query:\n\n{}\n", formatQuery(query));
      LOG.debug("GraphQL variables: {}", input.getVariables());
    }

    var executionInput = ExecutionInput.newExecutionInput()
        .query(query)
        .variables(input.getVariables())
        .dataLoaderRegistry(new DataLoaderRegistry())
        .build();

    return Mono.fromFuture(graphQL.executeAsync(executionInput));
  }

  private Mono<ServerResponse> handleResult(ExecutionResult result, ServerRequest request,
      Map<String, Object> inputParams) {
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
            .build();
      }

      Map<String, Object> resultData = result.getData();

      Object queryResultData = resultData.values()
          .iterator()
          .next();

      List<MediaType> acceptHeaders = request.headers()
          .accept();
      var template = getResponseTemplate(acceptHeaders);

      Mono<String> body;

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

      return bodyBuilder.body(body, String.class);
    }

    if (hasDirectiveValidationException(result)) {
      throw parameterValidationException("Validation of request parameters failed");
    }

    throw unwrapExceptionWhileNeeded(result);
  }

  private GraphQlErrorException unwrapExceptionWhileNeeded(ExecutionResult result) {
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

  private Mono<String> getResponseMapperBody(ServerRequest request, Map<String, Object> inputParams, Object data,
      ResponseTemplate template) {
    var uri = request.uri();

    if (Objects.nonNull(template.getResponseField())) {
      var responseWriteContext = createNewResponseWriteContext(template.getResponseField(), "root", data, inputParams,
          createNewDataStack(new ArrayDeque<>(), data, inputParams), uri);

      return jsonResponseMapper.toResponse(responseWriteContext);

    } else {
      var responseMapper = getResponseMapper(template.getMediaType(), data.getClass());
      return responseMapper.toResponse(data, httpMethodOperation);
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

  Mono<Map<String, Object>> resolveParameters(ServerRequest request) {
    var result = resolveUrlAndHeaderParameters(request);
    var requestBodyContext = this.responseSchemaContext.getRequestBodyContext();

    if (Objects.nonNull(requestBodyContext)) {
      var requestBody = resolveRequestBody(openApi, requestBodyContext.getRequestBodySchema());

      return requestBodyHandlerRouter.getRequestBodyHandler(requestBody)
          .getValues(request, requestBodyContext, requestBody, result)
          .map(values -> {
            result.putAll(values);
            return result;
          });
    }

    validateRequestBodyNonexistent(request);

    return Mono.just(addEvaluatedDwsParameters(result, responseSchemaContext.getDwsParameters(), request, jexlHelper));
  }

  protected Optional<QueryInput> getQueryInput(Map<String, Object> inputParams) {
    return graphQlQueryBuilder.toQueryInput(this.responseSchemaContext, inputParams);
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
    return resultData.get(responseSchemaContext.getDwsQuerySettings()
        .getQueryName()) != null;
  }
}
