package org.dotwebstack.framework.service.http;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.util.Objects;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlQueryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
class OpenApiConfiguration {

  private static final String FILENAME = "config/model/spec.yml";

  private final GraphQL graphQl;

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  private final GraphQlQueryBuilder queryBuilder;

  private final ObjectMapper objectMapper;

  public OpenApiConfiguration(GraphQL graphQl, TypeDefinitionRegistry typeDefinitionRegistry,
      GraphQlQueryBuilder queryBuilder, Jackson2ObjectMapperBuilder objectMapperBuiler) {
    this.graphQl = graphQl;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
    this.queryBuilder = queryBuilder;
    this.objectMapper = objectMapperBuiler.build();
  }

  @Bean
  public OpenAPI openApi() {
    return new OpenAPIV3Parser().read(FILENAME);
  }


  @Bean
  public RouterFunction<ServerResponse> route(OpenAPI openApi) {
    RouterFunctions.Builder routerFunctions = RouterFunctions.route();
    openApi.getPaths()
        .forEach((name, path) -> {
          if (Objects.nonNull(path.getGet())) {
            String dwsQuery = (String) path.getGet()
                .getExtensions()
                .get("x-dws-query");

            routerFunctions.add(RouterFunctions.route(GET(name).and(accept(MediaType.APPLICATION_JSON)),
                new CoreRequestHandler(graphQl, getQueryFieldDefinition(dwsQuery), queryBuilder, objectMapper)));
          }
        });
    return routerFunctions.build();
  }

  private FieldDefinition getQueryFieldDefinition(String dwsQuery) {
    ObjectTypeDefinition query = (ObjectTypeDefinition) this.typeDefinitionRegistry.getType("Query")
        .orElseThrow(() -> ExceptionHelper.invalidConfigurationException("Type 'Query' not found in GraphQL schema."));
    return query.getFieldDefinitions()
        .stream()
        .filter(fieldDefinition -> fieldDefinition.getName()
            .equals(dwsQuery))
        .findFirst()
        .orElseThrow(() -> ExceptionHelper
            .invalidConfigurationException("x-dws-query with value '{}' not found in GraphQL schema.", dwsQuery));
  }

}
