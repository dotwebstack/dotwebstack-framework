package org.dotwebstack.framework.service.openapi;

import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import graphql.GraphQL;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.service.openapi.helper.QueryFieldHelper;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
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

  private final GraphQL graphQl;

  private final ResponseMapper responseMapper;

  private final ParamHandlerRouter paramHandlerRouter;

  private final ResponseContextValidator responseContextValidator;

  private QueryFieldHelper queryFieldHelper;

  public OpenApiConfiguration(GraphQL graphQl, TypeDefinitionRegistry typeDefinitionRegistry,
      ResponseMapper responseMapper, ParamHandlerRouter paramHandlerRouter,
      ResponseContextValidator responseContextValidator) {
    this.graphQl = graphQl;
    this.paramHandlerRouter = paramHandlerRouter;
    this.responseMapper = responseMapper;
    this.responseContextValidator = responseContextValidator;
    this.queryFieldHelper = QueryFieldHelper.builder()
        .typeDefinitionRegistry(typeDefinitionRegistry)
        .graphQlFieldBuilder(new GraphQlFieldBuilder(typeDefinitionRegistry))
        .build();
  }

  @Bean
  public RouterFunction<ServerResponse> route(@NonNull OpenAPI openApi) {
    RouterFunctions.Builder routerFunctions = RouterFunctions.route();

    ResponseTemplateBuilder responseTemplateBuilder = ResponseTemplateBuilder.builder()
        .openApi(openApi)
        .build();

    openApi.getPaths()
        .forEach((name, path) -> Stream.of(path)
            .map(p -> getHttpMethodOperations(path, name))
            .peek(httpMethodOperations -> toOptionRouterFunction(httpMethodOperations).ifPresent(routerFunctions::add))
            .flatMap(Collection::stream)
            .map(httpMethodOperation -> toRouterFunctions(responseTemplateBuilder, httpMethodOperation))
            .forEach(routerFunctions::add));

    return routerFunctions.build();
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
      HttpMethodOperation httpMethodOperation) {
    List<ResponseTemplate> responseTemplates = responseTemplateBuilder.buildResponseTemplates(httpMethodOperation);

    GraphQlField graphQlField = queryFieldHelper.resolveGraphQlField(httpMethodOperation.getOperation());

    ResponseContext responseContext = new ResponseContext(graphQlField, responseTemplates,
        httpMethodOperation.getOperation()
            .getParameters() != null ? httpMethodOperation.getOperation()
                .getParameters() : Collections.emptyList());

    RequestPredicate requestPredicate = RequestPredicates.method(httpMethodOperation.getHttpMethod())
        .and(accept(MediaType.APPLICATION_JSON));

    return RouterFunctions.route(requestPredicate, new CoreRequestHandler(httpMethodOperation.getName(),
        responseContext, responseContextValidator, graphQl, responseMapper, paramHandlerRouter));

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
