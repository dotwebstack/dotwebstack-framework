package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;

import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.mapping.TypeValidator;
import org.springframework.stereotype.Component;

@Component
public class ResponseContextValidator {
  private final TypeValidator typeValidator = new TypeValidator();

  public void validate(@NonNull ResponseObject responseObject, @NonNull GraphQlField field,
      Set<Schema<?>> validatedSchemas, List<ResponseObject> parents) {
    String graphQlType = field.getType();
    ResponseSchema responseSchema = responseObject.getSchema();
    String oasType = responseSchema.getType();
    switch (oasType) {
      case ARRAY_TYPE:
        ResponseObject fieldTemplate = responseSchema.getItems()
            .get(0);
        if (!validatedSchemas.contains(fieldTemplate.getSchema()
            .getSchema())) {
          validate(fieldTemplate, field, validatedSchemas, copyAndAddToList(parents, responseObject));
        }
        break;
      case OBJECT_TYPE:
        List<ResponseObject> children = responseSchema.getChildren();
        if (Objects.nonNull(responseSchema.getSchema())) {
          validatedSchemas.add(responseSchema.getSchema());
        }

        GraphQlField usedField = getChildFieldWithName(field, responseObject.getIdentifier());

        children.stream()
            .filter(child -> Objects.isNull(child.getSchema()
                .getDwsExpr())
                && !validatedSchemas.contains(child.getSchema()
                    .getSchema()))
            .forEach(child -> {
              if (child.getSchema()
                  .isEnvelope()) {
                ResponseObject embedded = child.getSchema()
                    .getChildren()
                    .get(0);
                validate(embedded, usedField, validatedSchemas, copyAndAddToList(parents, responseObject, child));
              } else {
                GraphQlField graphQlChildField = usedField.getFields()
                    .stream()
                    .filter(childField -> childField.getName()
                        .equals(child.getIdentifier()))
                    .findFirst()
                    .orElseThrow(() -> invalidOpenApiConfigurationException(
                        "OAS field '{}' not found in matching GraphQl object '{}' for schema type '{}'",
                        getPath(parents, child.getIdentifier()), usedField.getName(), usedField.getType()));

                validate(child, graphQlChildField, validatedSchemas, copyAndAddToList(parents, responseObject));
              }
            });
        break;
      default:
        this.typeValidator.validateTypesGraphQlToOpenApi(oasType, graphQlType, responseObject.getIdentifier());
    }
  }

  private GraphQlField getChildFieldWithName(GraphQlField field, String name) {
    return field.getFields()
        .stream()
        .filter(childField -> Objects.equals(childField.getName(), name))
        .findFirst()
        .orElse(field);
  }

  private ArrayList<ResponseObject> copyAndAddToList(List<ResponseObject> list, ResponseObject... responseObject) {
    ArrayList<ResponseObject> responseObjects = new ArrayList<>(list);
    responseObjects.addAll(Arrays.asList(responseObject));
    return responseObjects;
  }

  private String getPath(List<ResponseObject> parents, String identifier) {
    StringJoiner joiner = new StringJoiner(".");
    int arrayCount = 0;
    for (ResponseObject parent : parents) {
      if (ARRAY_TYPE.equals(parent.getSchema()
          .getType())) {
        arrayCount++;
      } else {
        String parentIdentifier = parent.getIdentifier();
        if (parent.getSchema()
            .isEnvelope()) {
          parentIdentifier = "<" + parentIdentifier + ">";
        }
        if (arrayCount > 0) {
          parentIdentifier = parentIdentifier + IntStream.range(0, arrayCount)
              .mapToObj(i -> "[]")
              .collect(Collectors.joining());
          arrayCount = 0;
        }
        joiner.add(parentIdentifier);
      }
    }
    joiner.add(identifier);
    return joiner.toString();
  }
}
