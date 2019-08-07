package org.dotwebstack.framework.service.openapi;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.service.openapi.helper.QueryFieldHelper;
import org.dotwebstack.framework.service.openapi.param.ParamHandlerRouter;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@EnableConfigurationProperties(OpenApiProperties.class)
public class OpenApiConfiguration {

  private final GraphQL graphQl;

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  private final GraphQlFieldBuilder graphQlFieldBuilder;

  private final ObjectMapper objectMapper;

  private final OpenApiProperties properties;

  private final ParamHandlerRouter paramHandlerRouter;

  public OpenApiConfiguration(GraphQL graphQl, TypeDefinitionRegistry typeDefinitionRegistry,
      Jackson2ObjectMapperBuilder objectMapperBuilder, OpenApiProperties properties,
      ParamHandlerRouter paramHandlerRouter) {
    this.graphQl = graphQl;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
    this.paramHandlerRouter = paramHandlerRouter;
    this.graphQlFieldBuilder = new GraphQlFieldBuilder(this.typeDefinitionRegistry);
    this.objectMapper = objectMapperBuilder.build();
    this.properties = properties;
  }

  @Bean
  public OpenAPI openApi() {
    return new OpenAPIV3Parser().read(properties.getSpecificationFile());
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
          GraphQlField graphQlField = queryFieldHelper.resolveGraphQlField(path);
          if (Objects.nonNull(path.getGet())) {
            routerFunctions.add(
                toRouterFunction(path, responseTemplateBuilder, name, graphQlField, "get", path.getGet(), GET(name)));
          }
          if (Objects.nonNull(path.getPost())) {
            routerFunctions.add(toRouterFunction(path, responseTemplateBuilder, name, graphQlField, "post",
                path.getPost(), POST(name)));
          }
        });
    return routerFunctions.build();
  }

  protected RouterFunction<ServerResponse> toRouterFunction(PathItem pathItem,
      ResponseTemplateBuilder responseTemplateBuilder, String path, GraphQlField graphQlField, String methodName,
      Operation operation, RequestPredicate requestPredicate) {
    List<ResponseTemplate> responseTemplates =
        responseTemplateBuilder.buildResponseTemplates(path, methodName, operation);
    ResponseContext responseContext = new ResponseContext(graphQlField, responseTemplates,
        operation.getParameters() != null ? operation.getParameters() : Collections.emptyList());

    return RouterFunctions.route(requestPredicate.and(accept(MediaType.APPLICATION_JSON)),
        new CoreRequestHandler(path, responseContext, graphQl, objectMapper, paramHandlerRouter));
  }
}
