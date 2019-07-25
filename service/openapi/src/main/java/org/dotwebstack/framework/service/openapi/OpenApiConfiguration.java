package org.dotwebstack.framework.service.openapi;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.core.query.GraphQlQueryBuilder;
import org.dotwebstack.framework.service.openapi.response.ResponseContext;
import org.dotwebstack.framework.service.openapi.response.ResponseContextValidator;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseTemplate;
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
class OpenApiConfiguration {

  private final GraphQL graphQl;

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  private final GraphQlQueryBuilder queryBuilder;

  private final ObjectMapper objectMapper;

  private final OpenApiProperties properties;

  private final ResponseContextValidator responseContextValidator;

  public OpenApiConfiguration(GraphQL graphQl, TypeDefinitionRegistry typeDefinitionRegistry,
      GraphQlQueryBuilder queryBuilder, Jackson2ObjectMapperBuilder objectMapperBuiler, OpenApiProperties properties) {
    this.graphQl = graphQl;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
    this.queryBuilder = queryBuilder;
    this.objectMapper = objectMapperBuiler.build();
    this.properties = properties;
    this.responseContextValidator = new ResponseContextValidator();
  }

  @Bean
  public OpenAPI openApi() {
    return new OpenAPIV3Parser().read(properties.getSpecificationFile());
  }

  @Bean
  public RouterFunction<ServerResponse> route(OpenAPI openApi) {
    RouterFunctions.Builder routerFunctions = RouterFunctions.route();
    openApi.getPaths()
        .forEach((name, path) -> {
          if (Objects.nonNull(path.getGet())) {
            ResponseContext openApiContext = createOpenApiContext(openApi, name, "get", path.getGet());
            responseContextValidator.validate(openApiContext);

            routerFunctions.add(RouterFunctions.route(GET(name).and(accept(MediaType.APPLICATION_JSON)),
                new CoreRequestHandler(openApiContext, graphQl, queryBuilder, objectMapper)));
          }
          if (Objects.nonNull(path.getPost())) {
            ResponseContext openApiContext = createOpenApiContext(openApi, name, "post", path.getPost());
            responseContextValidator.validate(openApiContext);

            routerFunctions.add(RouterFunctions.route(GET(name).and(accept(MediaType.APPLICATION_JSON)),
                new CoreRequestHandler(openApiContext, graphQl, queryBuilder, objectMapper)));
          }
        });
    return routerFunctions.build();
  }

  private ResponseContext createOpenApiContext(OpenAPI openApi, String pathName, String methodName,
      Operation operation) {
    String dwsQuery = (String) operation.getExtensions()
        .get("x-dws-query");

    List<ResponseTemplate> responses = operation.getResponses()
        .entrySet()
        .stream()
        .flatMap(entry -> createResponses(openApi, entry.getKey(), entry.getValue(), pathName, methodName).stream())
        .collect(Collectors.toList());

    long successResponseCount = responses.stream()
        .filter(responseTemplate -> responseTemplate.isApplicable(200, 299))
        .count();
    if (successResponseCount != 1) {
      throw invalidConfigurationException(
          "Expected exactly one response within the 200 range for path '{}' with method '{}'.", pathName, methodName);
    }

    return ResponseContext.builder()
        .graphQlField(getGraphQlField(getQueryFieldDefinition(dwsQuery)))
        .responses(responses)
        .build();
  }

  private List<ResponseTemplate> createResponses(OpenAPI openApi, String responseCode, ApiResponse apiResponse,
      String pathName, String methodName) {
    validateMediaType(responseCode, apiResponse, pathName, methodName);
    return apiResponse.getContent()
        .entrySet()
        .stream()
        .map(entry -> createResponseObject(openApi, responseCode, entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  private void validateMediaType(String responseCode, ApiResponse apiResponse, String pathName, String methodName) {
    if (apiResponse.getContent()
        .keySet()
        .size() != 1) {
      throw ExceptionHelper.invalidConfigurationException(
          "Expected exactly one MediaType for path '{}' with method '{}' and response code '{}'.", pathName, methodName,
          responseCode);
    }
    List<String> unsupportedMediaTypes = apiResponse.getContent()
        .keySet()
        .stream()
        .filter(name -> !name.matches("application/.*\\+json"))
        .collect(Collectors.toList());
    if (!unsupportedMediaTypes.isEmpty()) {
      throw ExceptionHelper.invalidConfigurationException(
          "Unsupported MediaType(s) '{}' for path '{}' with method '{}' and response code '{}'.", unsupportedMediaTypes,
          pathName, methodName, responseCode);

    }
  }

  @SuppressWarnings("rawtypes")
  private ResponseTemplate createResponseObject(OpenAPI openApi, String responseCode, String mediatype,
      io.swagger.v3.oas.models.media.MediaType content) {
    String ref = content.getSchema()
        .get$ref();
    Schema schema = getSchemaReference(ref, openApi);

    if (Objects.isNull(schema)) {
      throw invalidConfigurationException("Schema '{}' not found in configuration.", ref);
    }

    ResponseObject root = createResponseObjectField(openApi, ref, schema, null);

    return ResponseTemplate.builder()
        .responseCode(Integer.parseInt(responseCode))
        .mediaType(mediatype)
        .responseObject(root)
        .build();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private ResponseObject createResponseObjectField(OpenAPI openApi, String identifier, Schema schema, Schema parent) {
    Map<String, Schema> schemaProperties = schema.getProperties();

    List<ResponseObject> children = null;
    if (Objects.nonNull(schemaProperties)) {
      children = schemaProperties.entrySet()
          .stream()
          .map(entry -> createResponseObjectField(openApi, entry.getKey(), entry.getValue(), schema))
          .collect(Collectors.toList());
    }

    List<ResponseObject> items = null;
    if (schema instanceof ArraySchema) {
      Schema item = ((ArraySchema) schema).getItems();
      String ref = item.get$ref();
      Schema child = getSchemaReference(ref, openApi);
      items = Collections.singletonList(createResponseObjectField(openApi, ref, child, null));
    }

    return ResponseObject.builder()
        .identifier(identifier)
        .type(schema.getType())
        .children(children)
        .items(items)
        .nillable(Objects.isNull(schema.getNullable()) ? Boolean.FALSE : schema.getNullable())
        .required(Objects.isNull(parent) || parent.getRequired()
            .contains(identifier))
        .build();
  }

  @SuppressWarnings("rawtypes")
  private Schema getSchemaReference(String ref, OpenAPI openApi) {
    String[] refPath = ref.split("/");
    return openApi.getComponents()
        .getSchemas()
        .get(refPath[refPath.length - 1]);
  }

  private FieldDefinition getQueryFieldDefinition(String dwsQuery) {
    ObjectTypeDefinition query = (ObjectTypeDefinition) this.typeDefinitionRegistry.getType("Query")
        .orElseThrow(() -> invalidConfigurationException("Type 'Query' not found in GraphQL schema."));
    return query.getFieldDefinitions()
        .stream()
        .filter(fieldDefinition -> fieldDefinition.getName()
            .equals(dwsQuery))
        .findFirst()
        .orElseThrow(
            () -> invalidConfigurationException("x-dws-query with value '{}' not found in GraphQL schema.", dwsQuery));
  }

  private GraphQlField getGraphQlField(FieldDefinition fieldDefinition) {
    return this.queryBuilder.toGraphQlField(fieldDefinition);
  }

}
