package org.dotwebstack.framework.service.openapi;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlFieldBuilder;
import org.dotwebstack.framework.service.openapi.helper.QueryFieldHelper;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplateBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
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

  private final ResponseContextValidator responseContextValidator;

  public OpenApiConfiguration(GraphQL graphQl, TypeDefinitionRegistry typeDefinitionRegistry,
      Jackson2ObjectMapperBuilder objectMapperBuilder, OpenApiProperties properties) {
    this.graphQl = graphQl;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
    this.graphQlFieldBuilder = new GraphQlFieldBuilder(this.typeDefinitionRegistry);
    this.objectMapper = objectMapperBuilder.build();
    this.properties = properties;
    this.responseContextValidator = new ResponseContextValidator();
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
            List<ResponseTemplate> responseTemplates =
                responseTemplateBuilder.buildResponseTemplates(name, "get", path.getGet());
            ResponseContext openApiContext = new ResponseContext(graphQlField, responseTemplates);

            responseContextValidator.validate(openApiContext);

            routerFunctions.add(RouterFunctions.route(GET(name).and(accept(MediaType.APPLICATION_JSON)),
                new CoreRequestHandler(openApiContext, graphQl, objectMapper)));
          }
          if (Objects.nonNull(path.getPost())) {
            List<ResponseTemplate> responseTemplates =
                responseTemplateBuilder.buildResponseTemplates(name, "post", path.getPost());
            ResponseContext openApiContext = new ResponseContext(graphQlField, responseTemplates);

            responseContextValidator.validate(openApiContext);

            routerFunctions.add(RouterFunctions.route(GET(name).and(accept(MediaType.APPLICATION_JSON)),
                new CoreRequestHandler(openApiContext, graphQl, objectMapper)));
          }
        });
    return routerFunctions.build();
  }
}
