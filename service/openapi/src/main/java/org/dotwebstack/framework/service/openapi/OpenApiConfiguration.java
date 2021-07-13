package org.dotwebstack.framework.service.openapi;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.isDwsOperation;
import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.graphql.GraphQlService;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.core.templating.TemplateResponseMapper;
import org.dotwebstack.framework.service.openapi.handler.CoreRequestHandler;
import org.dotwebstack.framework.service.openapi.handler.OpenApiRequestHandler;
import org.dotwebstack.framework.service.openapi.handler.OptionsRequestHandler;
import org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper;
import org.dotwebstack.framework.service.openapi.helper.QueryFieldHelper;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.dotwebstack.framework.service.openapi.mapping.JsonResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContextBuilder;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class OpenApiConfiguration {

  private static final String STATIC_ASSETS_LOCATION = "assets/";

  private final OpenAPI openApi;

  private final InputStream openApiStream;

  private final GraphQlService graphQl;

  private final List<ResponseMapper> responseMappers;

  private final JsonResponseMapper jsonResponseMapper;

  private final List<TemplateResponseMapper> templateResponseMappers;

  private final ParamHandlerRouter paramHandlerRouter;

  private final ResponseContextValidator responseContextValidator;

  private final RequestBodyHandlerRouter requestBodyHandlerRouter;

  private final JexlHelper jexlHelper;

  private final QueryFieldHelper queryFieldHelper;

  private final OpenApiProperties openApiProperties;

  private final EnvironmentProperties environmentProperties;

  public OpenApiConfiguration(OpenAPI openApi, @Qualifier("active") GraphQlService graphQl,
      TypeDefinitionRegistry typeDefinitionRegistry, List<ResponseMapper> responseMappers,
      JsonResponseMapper jsonResponseMapper, ParamHandlerRouter paramHandlerRouter, InputStream openApiStream,
      List<TemplateResponseMapper> templateResponseMappers, ResponseContextValidator responseContextValidator,
      RequestBodyHandlerRouter requestBodyHandlerRouter, OpenApiProperties openApiProperties, JexlEngine jexlEngine,
      EnvironmentProperties environmentProperties) {
    this.openApi = openApi;
    this.graphQl = graphQl;
    this.paramHandlerRouter = paramHandlerRouter;
    this.responseMappers = responseMappers;
    this.jsonResponseMapper = jsonResponseMapper;
    this.templateResponseMappers = templateResponseMappers;
    this.responseContextValidator = responseContextValidator;
    this.queryFieldHelper = QueryFieldHelper.builder()
        .typeDefinitionRegistry(typeDefinitionRegistry)
        .graphQlFieldBuilder(new GraphQlFieldBuilder(typeDefinitionRegistry))
        .build();
    this.openApiStream = openApiStream;
    this.requestBodyHandlerRouter = requestBodyHandlerRouter;
    this.openApiProperties = openApiProperties;
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.environmentProperties = environmentProperties;
  }

  Optional<RouterFunction<ServerResponse>> staticResourceRouter() {
    return ResourceLoaderUtils.getResource(STATIC_ASSETS_LOCATION)
        .map(staticResource -> RouterFunctions.resources("/" + STATIC_ASSETS_LOCATION + "**", staticResource));
  }

  public static class HttpAdviceTrait implements org.zalando.problem.spring.webflux.advice.http.HttpAdviceTrait {

  }

  @Bean
  public HttpAdviceTrait httpAdviceTrait() {
    return new HttpAdviceTrait();
  }

  @Bean
  public RouterFunction<ServerResponse> route(@NonNull OpenAPI openApi) {
    RouterFunctions.Builder routerFunctions = RouterFunctions.route();

    staticResourceRouter().ifPresent(routerFunctions::add);

    var responseTemplateBuilder = ResponseTemplateBuilder.builder()
        .openApi(openApi)
        .xdwsStringTypes(openApiProperties.getXdwsStringTypes())
        .build();
    var requestBodyContextBuilder = new RequestBodyContextBuilder(openApi);
    openApi.getPaths()
        .forEach((name, path) -> {
          Optional<List<HttpMethodOperation>> operations = Optional.of(path)
              .map(p -> getHttpMethodOperations(path, name));

          operations.flatMap(this::toOptionRouterFunction)
              .ifPresent(routerFunctions::add);

          operations.ifPresent(httpMethodOperations -> Stream.of(httpMethodOperations)
              .flatMap(Collection::stream)
              .map(httpMethodOperation -> toRouterFunctions(responseTemplateBuilder, requestBodyContextBuilder,
                  httpMethodOperation))
              .forEach(routerFunctions::add));

        });

    addOpenApiSpecEndpoints(routerFunctions, openApiStream);
    return routerFunctions.build();
  }

  protected void addOpenApiSpecEndpoints(RouterFunctions.Builder routerFunctions, @NonNull InputStream openApiStream) {
    RequestPredicate getPredicate = RequestPredicates.method(HttpMethod.GET)
        .and(RequestPredicates.path(openApiProperties.getApiDocPublicationPath()))
        .and(accept(MediaType.APPLICATION_JSON));

    routerFunctions.add(RouterFunctions.route(OPTIONS(openApiProperties.getApiDocPublicationPath()),
        new OptionsRequestHandler(List.of(HttpMethod.GET))));
    routerFunctions.add(RouterFunctions.route(getPredicate, new OpenApiRequestHandler(openApiStream)));
  }

  private List<HttpMethodOperation> getHttpMethodOperations(PathItem pathItem, String name) {
    HttpMethodOperation.HttpMethodOperationBuilder builder = HttpMethodOperation.builder()
        .name(name);

    List<HttpMethodOperation> httpMethodOperations = new ArrayList<>();

    if (Objects.nonNull(pathItem.getGet())) {
      httpMethodOperations.add(builder.httpMethod(HttpMethod.GET)
          .operation(pathItem.getGet())
          .build());
    }
    if (Objects.nonNull(pathItem.getPost())) {
      httpMethodOperations.add(builder.httpMethod(HttpMethod.POST)
          .operation(pathItem.getPost())
          .build());
    }

    return httpMethodOperations.stream()
        .filter(httpMethodOperation -> isDwsOperation(httpMethodOperation.getOperation()))
        .collect(Collectors.toList());
  }

  protected RouterFunction<ServerResponse> toRouterFunctions(ResponseTemplateBuilder responseTemplateBuilder,
      RequestBodyContextBuilder requestBodyContextBuilder, HttpMethodOperation httpMethodOperation) {
    var requestBodyContext = requestBodyContextBuilder.buildRequestBodyContext(httpMethodOperation.getOperation()
        .getRequestBody());

    List<ResponseTemplate> responseTemplates = responseTemplateBuilder.buildResponseTemplates(httpMethodOperation);

    Optional<GraphQlField> graphQlField = queryFieldHelper.resolveGraphQlField(httpMethodOperation.getOperation());
    List<String> requiredFields = DwsExtensionHelper.getDwsRequiredFields(httpMethodOperation.getOperation());

    var responseSchemaContext = ResponseSchemaContext.builder()
        .graphQlField(graphQlField.orElse(null))
        .requiredFields(Objects.nonNull(requiredFields) ? requiredFields : Collections.emptyList())
        .responses(responseTemplates)
        .parameters(httpMethodOperation.getOperation()
            .getParameters() != null ? httpMethodOperation.getOperation()
                .getParameters() : Collections.emptyList())
        .dwsParameters(DwsExtensionHelper.getDwsQueryParameters(httpMethodOperation.getOperation()))
        .requestBodyContext(requestBodyContext)
        .build();

    var requestPredicate = RequestPredicates.method(httpMethodOperation.getHttpMethod())
        .and(RequestPredicates.path(httpMethodOperation.getName()));

    validateTemplateResponseMapper(responseTemplates);
    var templateResponseMapper = getTemplateResponseMapper();

    var coreRequestHandler = new CoreRequestHandler(openApi, httpMethodOperation.getName(), responseSchemaContext,
        responseContextValidator, graphQl, responseMappers, jsonResponseMapper, templateResponseMapper,
        paramHandlerRouter, requestBodyHandlerRouter, jexlHelper, environmentProperties);

    responseTemplates.stream()
        .map(ResponseTemplate::getResponseCode)
        .map(HttpStatus::valueOf)
        .filter(httpStatus -> !httpStatus.is3xxRedirection())
        .findFirst()
        .ifPresent(i -> coreRequestHandler.validateSchema());

    return RouterFunctions.route(requestPredicate, coreRequestHandler);
  }

  private TemplateResponseMapper getTemplateResponseMapper() {
    if (templateResponseMappers.isEmpty()) {
      return null;
    }
    return templateResponseMappers.get(0);
  }

  private void validateTemplateResponseMapper(List<ResponseTemplate> responseTemplates) {
    boolean usesTemplating = responseTemplates.stream()
        .anyMatch(ResponseTemplate::usesTemplating);

    if (usesTemplating && templateResponseMappers.isEmpty()) {
      throw invalidOpenApiConfigurationException("Configured a template, but templating module not used");
    }

    int size = templateResponseMappers.size();
    if (size > 1) {
      throw invalidConfigurationException("More than 1 templateResponseMapper configured, found: {}", size);
    }
  }

  protected Optional<RouterFunction<ServerResponse>> toOptionRouterFunction(
      List<HttpMethodOperation> httpMethodOperations) {
    if (httpMethodOperations == null || httpMethodOperations.isEmpty()) {
      return Optional.empty();
    }

    List<HttpMethod> httpMethods = httpMethodOperations.stream()
        .map(HttpMethodOperation::getHttpMethod)
        .collect(Collectors.toList());

    return Optional.of(RouterFunctions.route(OPTIONS(httpMethodOperations.get(0)
        .getName()), new OptionsRequestHandler(httpMethods)));
  }
}
