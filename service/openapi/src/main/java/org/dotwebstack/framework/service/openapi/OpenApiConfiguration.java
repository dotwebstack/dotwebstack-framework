package org.dotwebstack.framework.service.openapi;

import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import graphql.GraphQL;
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
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.service.openapi.handler.CoreRequestHandler;
import org.dotwebstack.framework.service.openapi.handler.OpenApiRequestHandler;
import org.dotwebstack.framework.service.openapi.handler.OptionsRequestHandler;
import org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper;
import org.dotwebstack.framework.service.openapi.helper.QueryFieldHelper;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.requestbody.RequestBodyHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContextBuilder;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseSchemaContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class OpenApiConfiguration {

  private final OpenAPI openApi;

  private final InputStream openApiStream;

  private final GraphQL graphQl;

  private final ResponseMapper responseMapper;

  private final ParamHandlerRouter paramHandlerRouter;

  private final ResponseContextValidator responseContextValidator;

  private final RequestBodyHandlerRouter requestBodyHandlerRouter;

  private final JexlHelper jexlHelper;

  private QueryFieldHelper queryFieldHelper;

  private OpenApiProperties openApiProperties;

  public OpenApiConfiguration(OpenAPI openApi, GraphQL graphQl, TypeDefinitionRegistry typeDefinitionRegistry,
      ResponseMapper responseMapper, ParamHandlerRouter paramHandlerRouter, InputStream openApiStream,
      ResponseContextValidator responseContextValidator, RequestBodyHandlerRouter requestBodyHandlerRouter,
      OpenApiProperties openApiProperties, JexlEngine jexlEngine) {
    this.openApi = openApi;
    this.graphQl = graphQl;
    this.paramHandlerRouter = paramHandlerRouter;
    this.responseMapper = responseMapper;
    this.responseContextValidator = responseContextValidator;
    this.queryFieldHelper = QueryFieldHelper.builder()
        .typeDefinitionRegistry(typeDefinitionRegistry)
        .graphQlFieldBuilder(new GraphQlFieldBuilder(typeDefinitionRegistry))
        .build();
    this.openApiStream = openApiStream;
    this.requestBodyHandlerRouter = requestBodyHandlerRouter;
    this.openApiProperties = openApiProperties;
    this.jexlHelper = new JexlHelper(jexlEngine);
  }

  @Bean
  public RouterFunction<ServerResponse> route(@NonNull OpenAPI openApi) {
    RouterFunctions.Builder routerFunctions = RouterFunctions.route();

    ResponseTemplateBuilder responseTemplateBuilder = ResponseTemplateBuilder.builder()
        .openApi(openApi)
        .xdwsStringTypes(openApiProperties.getXdwsStringTypes())
        .build();
    RequestBodyContextBuilder requestBodyContextBuilder = new RequestBodyContextBuilder(openApi);
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
        .and(RequestPredicates.path(""))
        .and(accept(MediaType.APPLICATION_JSON));

    routerFunctions.add(RouterFunctions.route(OPTIONS(""), new OptionsRequestHandler(List.of(HttpMethod.GET))));
    routerFunctions.add(RouterFunctions.route(getPredicate, new OpenApiRequestHandler(openApiStream)));
  }

  private List<HttpMethodOperation> getHttpMethodOperations(PathItem pathItem, String name) {
    HttpMethodOperation.HttpMethodOperationBuilder builder = HttpMethodOperation.builder()
        .name(name);

    List<HttpMethodOperation> result = new ArrayList<>();

    if (Objects.nonNull(pathItem.getGet())) {
      result.add(builder.httpMethod(HttpMethod.GET)
          .operation(pathItem.getGet())
          .build());
    }
    if (Objects.nonNull(pathItem.getPost())) {
      result.add(builder.httpMethod(HttpMethod.POST)
          .operation(pathItem.getPost())
          .build());
    }
    return result;
  }

  protected RouterFunction<ServerResponse> toRouterFunctions(ResponseTemplateBuilder responseTemplateBuilder,
      RequestBodyContextBuilder requestBodyContextBuilder, HttpMethodOperation httpMethodOperation) {
    RequestBodyContext requestBodyContext =
        requestBodyContextBuilder.buildRequestBodyContext(httpMethodOperation.getOperation()
            .getRequestBody());

    List<ResponseTemplate> responseTemplates = responseTemplateBuilder.buildResponseTemplates(httpMethodOperation);

    GraphQlField graphQlField = queryFieldHelper.resolveGraphQlField(httpMethodOperation.getOperation());

    ResponseSchemaContext responseSchemaContext = new ResponseSchemaContext(graphQlField, responseTemplates,
        httpMethodOperation.getOperation()
            .getParameters() != null ? httpMethodOperation.getOperation()
                .getParameters() : Collections.emptyList(),
        DwsExtensionHelper.getDwsQueryParameters(httpMethodOperation.getOperation()), requestBodyContext);

    RequestPredicate requestPredicate = RequestPredicates.method(httpMethodOperation.getHttpMethod())
        .and(RequestPredicates.path(httpMethodOperation.getName()))
        .and(accept(MediaType.APPLICATION_JSON));

    return RouterFunctions.route(requestPredicate,
        new CoreRequestHandler(openApi, httpMethodOperation.getName(), responseSchemaContext, responseContextValidator,
            graphQl, responseMapper, paramHandlerRouter, requestBodyHandlerRouter, jexlHelper));
  }

  protected Optional<RouterFunction<ServerResponse>> toOptionRouterFunction(
      @NonNull List<HttpMethodOperation> httpMethodOperations) {
    if (httpMethodOperations.size() == 0) {
      return Optional.empty();
    }

    List<HttpMethod> httpMethods = httpMethodOperations.stream()
        .map(HttpMethodOperation::getHttpMethod)
        .collect(Collectors.toList());

    return Optional.of(RouterFunctions.route(OPTIONS(httpMethodOperations.get(0)
        .getName()), new OptionsRequestHandler(httpMethods)));
  }
}
