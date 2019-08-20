package org.dotwebstack.framework.service.openapi.param;

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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.query.GraphQlArgument;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.helper.SchemaUtils;
import org.dotwebstack.framework.service.openapi.mapping.TypeValidator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

@Component
public class RequestBodyHandler {

  private OpenAPI openApi;

  private TypeDefinitionRegistry typeDefinitionRegistry;

  private TypeValidator typeValidator;

  public RequestBodyHandler(OpenAPI openApi, TypeDefinitionRegistry typeDefinitionRegistry) {
    this.openApi = openApi;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
    this.typeValidator = new TypeValidator();
  }

  public Optional<Object> getValue(@NonNull ServerRequest request, @NonNull RequestBody requestBody) {
    return null;
  }

  @SuppressWarnings("rawtypes")
  public void validate(@NonNull GraphQlField graphQlField, @NonNull RequestBody parameter, @NonNull String pathName) {
    parameter.getContent()
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
          .filter(a -> a.getName()
              .equals(name))
          .findFirst()
          .orElseThrow(() -> ExceptionHelper.invalidConfigurationException(
              "OAS property '{}' for path '{}' was not found as a " + "GraphQL argument on field '{}'", name, pathName,
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
      // handle object
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
              .orElseThrow(() -> ExceptionHelper.invalidConfigurationException(""));
      Map<String, Schema> properties = schema.getProperties();
      properties.forEach((name, childSchema) -> {
        InputValueDefinition inputValueDefinition = typeDefinition.getInputValueDefinitions()
            .stream()
            .filter(iv -> iv.getName()
                .equals(name))
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
      // handle array
      if (!(unwrapped instanceof ListType)) {
        throw ExceptionHelper
            .invalidConfigurationException("Property '{}' with OAS array type cannot be mapped to GraphQL type '{}', it"
                + " should be mapped to type '{}'.", propertyName, unwrapped.getClass(), ListType.class.getName());
      }
      Schema<?> itemSchema = ((ArraySchema) schema).getItems();
      validate(propertyName, itemSchema, TypeHelper.getBaseType(unwrapped), pathName);
    } else {
      // handle scalar
      this.typeValidator.validateTypes(schema.getType(), TypeHelper.getTypeName(unwrapped), propertyName);
    }
  }
}
