package org.dotwebstack.framework.service.openapi;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import graphql.GraphQL;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.service.openapi.helper.QueryFieldHelper;
import org.dotwebstack.framework.service.openapi.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.param.RequestBodyHandler;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class OpenApiConfiguration {

  private final GraphQL graphQl;

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  private final GraphQlFieldBuilder graphQlFieldBuilder;

  private final ResponseMapper responseMapper;

  private final ParamHandlerRouter paramHandlerRouter;

  private final ResponseContextValidator responseContextValidator;

  private final RequestBodyHandler requestBodyHandler;

  public OpenApiConfiguration(GraphQL graphQl, TypeDefinitionRegistry typeDefinitionRegistry,
      ResponseMapper responseMapper, ParamHandlerRouter paramHandlerRouter,
      ResponseContextValidator responseContextValidator, RequestBodyHandler requestBodyHandler) {
    this.graphQl = graphQl;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
    this.paramHandlerRouter = paramHandlerRouter;
    this.graphQlFieldBuilder = new GraphQlFieldBuilder(this.typeDefinitionRegistry);
    this.responseMapper = responseMapper;
    this.responseContextValidator = responseContextValidator;
    this.requestBodyHandler = requestBodyHandler;
  }

  @Bean
  public RouterFunction<ServerResponse> route(@NonNull OpenAPI openApi) {
    RouterFunctions.Builder routerFunctions = RouterFunctions.route();
    QueryFieldHelper queryFieldHelper = QueryFieldHelper.builder()
        .typeDefinitionRegistry(this.typeDefinitionRegistry)
        .graphQlFieldBuilder(this.graphQlFieldBuilder)
        .build();
    ResponseTemplateBuilder responseTemplateBuilder = ResponseTemplateBuilder.builder()
        .openApi(openApi)
        .build();
    openApi.getPaths()
        .forEach((name, path) -> {
          if (Objects.nonNull(path.getGet())) {
            routerFunctions.add(toRouterFunction(responseTemplateBuilder, name,
                queryFieldHelper.resolveGraphQlField(path.getGet()), "get", path.getGet(), GET(name)));
          }
          if (Objects.nonNull(path.getPost())) {
            routerFunctions.add(toRouterFunction(responseTemplateBuilder, name,
                queryFieldHelper.resolveGraphQlField(path.getPost()), "post", path.getPost(), POST(name)));
          }
        });
    return routerFunctions.build();
  }

  protected RouterFunction<ServerResponse> toRouterFunction(ResponseTemplateBuilder responseTemplateBuilder,
      String path, GraphQlField graphQlField, String methodName, Operation operation,
      RequestPredicate requestPredicate) {
    List<ResponseTemplate> responseTemplates =
        responseTemplateBuilder.buildResponseTemplates(path, methodName, operation);
    ResponseContext responseContext = new ResponseContext(graphQlField, responseTemplates,
        operation.getParameters() != null ? operation.getParameters() : Collections.emptyList(),
        operation.getRequestBody());

    return RouterFunctions.route(requestPredicate.and(accept(MediaType.APPLICATION_JSON)), new CoreRequestHandler(path,
        responseContext, responseContextValidator, graphQl, responseMapper, paramHandlerRouter, requestBodyHandler));
  }
}
