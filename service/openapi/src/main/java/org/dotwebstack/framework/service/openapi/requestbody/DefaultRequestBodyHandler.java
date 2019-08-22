package org.dotwebstack.framework.service.openapi.requestbody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.query.GraphQlArgument;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper;
import org.dotwebstack.framework.service.openapi.helper.JsonNodeUtils;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.helper.SchemaUtils;
import org.dotwebstack.framework.service.openapi.mapping.TypeValidator;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

public class DefaultRequestBodyHandler implements RequestBodyHandler {

  private OpenAPI openApi;

  private TypeDefinitionRegistry typeDefinitionRegistry;

  private TypeValidator typeValidator;

  public DefaultRequestBodyHandler(OpenAPI openApi, TypeDefinitionRegistry typeDefinitionRegistry) {
    this.openApi = openApi;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
    this.typeValidator = new TypeValidator();
  }

  @Override
  public Optional<Object> getValue(@NonNull ServerRequest request, @NonNull RequestBodyContext requestBodyContext,
      Map<String, Object> parameterMap) throws BadRequestException {
    Mono<String> mono = request.bodyToMono(String.class);
    String value = mono.block();
    if (Objects.isNull(value) && requestBodyContext.getRequestBodySchema()
        .getRequired()) {
      throw OpenApiExceptionHelper.badRequestException("Request body required but not found.");
    } else if (Objects.isNull(value)) {
      return Optional.empty();
    } else {
      validateContentType(request);
      try {
        JsonNode node = new ObjectMapper().reader()
            .readTree(value);
        return Optional.ofNullable(JsonNodeUtils.toObject(node));
      } catch (IOException e) {
        throw ExceptionHelper.illegalArgumentException("Could not parse request body as JSON: {}.", e.getMessage());
      }
    }
  }

  @SuppressWarnings("rawtypes")
  public void validate(@NonNull GraphQlField graphQlField, @NonNull RequestBody requestBody, @NonNull String pathName) {
    requestBody.getContent()
        .forEach((key, mediaType) -> {
          Schema schema = mediaType.getSchema();
          if (schema.get$ref() != null) {
            schema = SchemaUtils.getSchemaReference(schema.get$ref(), openApi);
          }
          String type = schema.getType();
          if (!Objects.equals(type, OasConstants.OBJECT_TYPE)) {
            throw OpenApiExceptionHelper
                .invalidConfigurationException("Schema type '{}' not supported for request body.", type);
          }
          validate(schema, graphQlField, pathName);
        });
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void validate(Schema schema, GraphQlField graphQlField, String pathName) {
    Map<String, Schema> properties = schema.getProperties();
    properties.forEach((name, propertySchema) -> {
      GraphQlArgument argument = graphQlField.getArguments()
          .stream()
          .filter(a -> Objects.equals(a.getName(), name))
          .findFirst()
          .orElseThrow(() -> ExceptionHelper.invalidConfigurationException(
              "OAS property '{}' for path '{}' was not found as a " + "GraphQL argument on field '{}'.", name, pathName,
              graphQlField.getName()));
      validate(name, propertySchema, argument.getType(), pathName);
    });
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void validate(String propertyName, Schema propertySchema, Type graphQlType, String pathName) {
    Schema schema = propertySchema.get$ref() != null ? SchemaUtils.getSchemaReference(propertySchema.get$ref(), openApi)
        : propertySchema;
    Type unwrapped = TypeHelper.unwrapNonNullType(graphQlType);
    if (OasConstants.OBJECT_TYPE.equals(schema.getType())) {
      if (!(unwrapped instanceof TypeName)) {
        throw ExceptionHelper.invalidConfigurationException(
            "Property '{}' with OAS object type cannot be mapped to GraphQL type '{}', "
                + "it should be mapped to type '{}'.",
            propertyName, unwrapped.getClass()
                .getName(),
            TypeName.class.getName());
      }
      InputObjectTypeDefinition typeDefinition =
          (InputObjectTypeDefinition) this.typeDefinitionRegistry.getType(unwrapped)
              .orElseThrow(() -> ExceptionHelper
                  .invalidConfigurationException("Could not find type definition of GraphQL type '{}'", unwrapped));
      Map<String, Schema> properties = schema.getProperties();
      properties.forEach((name, childSchema) -> {
        InputValueDefinition inputValueDefinition = typeDefinition.getInputValueDefinitions()
            .stream()
            .filter(iv -> Objects.equals(iv.getName(), name))
            .findFirst()
            .orElseThrow(
                () -> ExceptionHelper
                    .invalidConfigurationException(
                        "OAS property '{}' for path '{}' was not found as a "
                            + "GraphQL intput value on input object type '{}'",
                        name, pathName, typeDefinition.getName()));
        validate(name, childSchema, inputValueDefinition.getType(), pathName);
      });
    } else if (OasConstants.ARRAY_TYPE.equals(schema.getType())) {
      if (!(unwrapped instanceof ListType)) {
        throw ExceptionHelper
            .invalidConfigurationException("Property '{}' with OAS array type cannot be mapped to GraphQL type '{}', it"
                + " should be mapped to type '{}'.", propertyName, unwrapped.getClass(), ListType.class.getName());
      }
      Schema<?> itemSchema = ((ArraySchema) schema).getItems();
      validate(propertyName, itemSchema, TypeHelper.getBaseType(unwrapped), pathName);
    } else {
      this.typeValidator.validateTypesOpenApiToGraphQ(schema.getType(), TypeHelper.getTypeName(unwrapped),
          propertyName);
    }
  }

  private void validateContentType(ServerRequest request) throws BadRequestException {
    List<String> contentTypeHeaders = request.headers()
        .header(OasConstants.HEADER_CONTENT_TYPE);
    if (contentTypeHeaders.size() != 1) {
      throw OpenApiExceptionHelper.badRequestException("Expected exactly 1 '{}' header but found {}.",
          OasConstants.HEADER_CONTENT_TYPE, contentTypeHeaders.size());
    } else if (!Objects.equals(MediaType.APPLICATION_JSON.toString(), contentTypeHeaders.get(0))) {
      throw new UnsupportedMediaTypeException(MediaType.parseMediaType(contentTypeHeaders.get(0)),
          Arrays.asList(MediaType.APPLICATION_JSON));
    }
  }

  @Override
  public boolean supports(@NonNull RequestBodyContext requestBodyContext) {
    return true;
  }

  protected OpenAPI getOpenApi() {
    return this.openApi;
  }
}
