package org.dotwebstack.framework.service.openapi.requestbody;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.badRequestException;
import static org.dotwebstack.framework.service.openapi.helper.SchemaResolver.resolveSchema;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.query.GraphQlArgument;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.BadRequestException;
import org.dotwebstack.framework.service.openapi.helper.JsonNodeUtils;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.mapping.TypeValidator;
import org.dotwebstack.framework.service.openapi.response.RequestBodyContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

@Component
public class DefaultRequestBodyHandler implements RequestBodyHandler {

  private OpenAPI openApi;

  private TypeDefinitionRegistry typeDefinitionRegistry;

  private TypeValidator typeValidator;

  private ObjectMapper objectMapper;

  public DefaultRequestBodyHandler(@NonNull OpenAPI openApi, @NonNull TypeDefinitionRegistry typeDefinitionRegistry,
      @NonNull Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
    this.openApi = openApi;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
    this.typeValidator = new TypeValidator();
    this.objectMapper = jackson2ObjectMapperBuilder.build();
  }

  @Override
  public Map<String, Object> getValues(@NonNull ServerRequest request, @NonNull RequestBodyContext requestBodyContext,
      @NonNull RequestBody requestBody, @NonNull Map<String, Object> parameterMap) throws BadRequestException {
    Mono<String> mono = request.bodyToMono(String.class);
    String value = mono.block();

    if (Objects.isNull(value) && Boolean.TRUE.equals(requestBody.getRequired())) {
      throw badRequestException("Request body required but not found.");
    } else if (Objects.isNull(value)) {
      return Collections.emptyMap();
    } else {
      try {
        JsonNode node = objectMapper.reader()
            .readTree(value);
        Map<String, Object> result = new HashMap<>();
        node.fields()
            .forEachRemaining(field -> result.put(field.getKey(), JsonNodeUtils.toObject(field.getValue())));
        return result;
      } catch (IOException e) {
        throw illegalArgumentException("Could not parse request body as JSON: {}.", e.getMessage());
      }
    }
  }

  public void validate(@NonNull GraphQlField graphQlField, @NonNull RequestBody requestBody, @NonNull String pathName) {
    requestBody.getContent()
        .forEach((key, mediaType) -> {
          Schema<?> schema = resolveSchema(openApi, mediaType.getSchema());
          String type = schema.getType();
          if (!Objects.equals(type, OasConstants.OBJECT_TYPE)) {
            throw invalidConfigurationException("Schema type '{}' not supported for request body.", type);
          }
          validate(schema, graphQlField, pathName);
        });
  }

  @SuppressWarnings("rawtypes")
  private void validate(Schema<?> schema, GraphQlField graphQlField, String pathName) {
    Map<String, Schema> properties = schema.getProperties();
    properties.forEach((name, propertySchema) -> {
      GraphQlArgument argument = graphQlField.getArguments()
          .stream()
          .filter(a -> Objects.equals(a.getName(), name))
          .findFirst()
          .orElseThrow(() -> invalidConfigurationException(
              "OAS property '{}' for path '{}' was not found as a " + "GraphQL argument on field '{}'.", name, pathName,
              graphQlField.getName()));
      validate(name, propertySchema, argument.getType(), pathName);
    });
  }

  @SuppressWarnings("rawtypes")
  private void validate(String propertyName, Schema<?> propertySchema, Type graphQlType, String pathName) {
    Schema<?> schema = resolveSchema(openApi, propertySchema);
    Type unwrapped = TypeHelper.unwrapNonNullType(graphQlType);
    if (OasConstants.OBJECT_TYPE.equals(schema.getType())) {
      if (!(unwrapped instanceof TypeName)) {
        throw invalidConfigurationException(
            "Property '{}' with OAS object type cannot be mapped to GraphQL type '{}', "
                + "it should be mapped to type '{}'.",
            propertyName, unwrapped.getClass()
                .getName(),
            TypeName.class.getName());
      }
      InputObjectTypeDefinition typeDefinition = (InputObjectTypeDefinition) this.typeDefinitionRegistry
          .getType(unwrapped)
          .orElseThrow(
              () -> invalidConfigurationException("Could not find type definition of GraphQL type '{}'", unwrapped));
      validateProperties(pathName, schema, typeDefinition);
    } else if (OasConstants.ARRAY_TYPE.equals(schema.getType())) {
      if (!(unwrapped instanceof ListType)) {
        throw invalidConfigurationException(
            "Property '{}' with OAS array type cannot be mapped to GraphQL type '{}', it"
                + " should be mapped to type '{}'.",
            propertyName, unwrapped.getClass(), ListType.class.getName());
      }
      Schema<?> itemSchema = ((ArraySchema) schema).getItems();
      validate(propertyName, itemSchema, TypeHelper.getBaseType(unwrapped), pathName);
    } else {
      this.typeValidator.validateTypesOpenApiToGraphQ(schema.getType(), TypeHelper.getTypeName(unwrapped),
          propertyName);
    }
  }

  @SuppressWarnings("rawtypes")
  private void validateProperties(String pathName, Schema<?> schema, InputObjectTypeDefinition typeDefinition) {
    Map<String, Schema> properties = schema.getProperties();
    properties.forEach((name, childSchema) -> {
      InputValueDefinition inputValueDefinition = typeDefinition.getInputValueDefinitions()
          .stream()
          .filter(iv -> Objects.equals(iv.getName(), name))
          .findFirst()
          .orElseThrow(() -> invalidConfigurationException(
              "OAS property '{}' for path '{}' was not found as a " + "GraphQL intput value on input object type '{}'",
              name, pathName, typeDefinition.getName()));
      validate(name, childSchema, inputValueDefinition.getType(), pathName);
    });
  }

  @Override
  public boolean supports(@NonNull RequestBody requestBody) {
    return Objects.nonNull(requestBody.getContent()
        .get(MediaType.APPLICATION_JSON));
  }
}
