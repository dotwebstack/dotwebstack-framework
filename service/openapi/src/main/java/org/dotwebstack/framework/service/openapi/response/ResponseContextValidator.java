package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.invalidOpenApiConfigurationException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.IntStream;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.mapping.TypeValidator;
import org.springframework.stereotype.Component;

@Component
public class ResponseContextValidator {
  private final TypeValidator typeValidator = new TypeValidator();

  public void validate(@NonNull ResponseObject responseObject, @NonNull GraphQlField field) {
    this.validate(responseObject, field, new HashSet<>(), new ArrayList<>(), field.isListType());
  }

  private void validate(ResponseObject responseObject, GraphQlField field, Set<String> validatedReferences,
      List<ResponseObject> parents, boolean isArrayRoot) {
    String graphQlType = field.getType();
    SchemaSummary summary = responseObject.getSummary();
    String oasType = summary.getType();

    switch (oasType) {
      case ARRAY_TYPE:
        ResponseObject item = summary.getItems()
            .get(0);
        if (!validatedReferences.contains(item.getSummary()
            .getRef())) {
          ArrayList<ResponseObject> copy = copyAndAddToList(parents, responseObject);
          if (item.getSummary()
              .isEnvelope()
              || !Objects.isNull(item.getSummary()
                  .getDwsExpr())) {
            validate(item, field, validatedReferences, copy, isArrayRoot);
          } else {
            validate(item, getChildFieldWithName(isArrayRoot, field, responseObject, copy), validatedReferences, copy,
                false);
          }
        }
        break;
      case OBJECT_TYPE:
        if (Objects.nonNull(summary.getRef())) {
          validatedReferences.add(summary.getRef());
        }

        summary.getChildren()
            .stream()
            .filter(child -> Objects.isNull(child.getSummary()
                .getDwsExpr())
                && !validatedReferences.contains(child.getSummary()
                    .getRef()))
            .forEach(child -> {
              SchemaSummary childSummary = child.getSummary();

              ArrayList<ResponseObject> copy = copyAndAddToList(parents, responseObject);
              if (Objects.equals(childSummary.getType(), ARRAY_TYPE) || childSummary.isEnvelope()
                  || !Objects.isNull(summary.getDwsExpr())) {
                validate(child, field, validatedReferences, copy, isArrayRoot);
              } else {
                validate(child, getChildFieldWithName(isArrayRoot, field, child, copy), validatedReferences, copy,
                    false);
              }
            });
        break;
      default:
        // Skip 'static' paths in the OAS specification. (all nodes are envelopes)
        if (responseObject.getSummary()
            .isEnvelope()) {
          return;
        }
        if (!Objects.equals(field.getName(), responseObject.getIdentifier())) {
          throw invalidOpenApiConfigurationException(
              "OAS field '{}' does not match with GraphQl object '{}' for schema type '{}'",
              getPath(parents, responseObject.getIdentifier()), field.getName(), field.getType());
        }
        this.typeValidator.validateTypesGraphQlToOpenApi(oasType, graphQlType, responseObject.getIdentifier());
    }
  }

  private GraphQlField getChildFieldWithName(boolean isRoot, GraphQlField field, ResponseObject responseObject,
      List<ResponseObject> parents) {
    if (isRoot && Objects.equals(responseObject.getSummary()
        .getType(), ARRAY_TYPE)) {
      return field;
    }
    return field.getFields()
        .stream()
        .filter(childField -> Objects.equals(childField.getName(), responseObject.getIdentifier()))
        .findFirst()
        .orElseThrow(() -> invalidOpenApiConfigurationException(
            "OAS field '{}' does not match with GraphQl object '{}' for schema type '{}'",
            getPath(parents, responseObject.getIdentifier()), field.getName(), field.getType()));

  }

  private ArrayList<ResponseObject> copyAndAddToList(List<ResponseObject> list, ResponseObject responseObject) {
    ArrayList<ResponseObject> responseObjects = new ArrayList<>(list);
    responseObjects.add(responseObject);
    return responseObjects;
  }

  private String getPath(List<ResponseObject> parents, String identifier) {
    StringJoiner joiner = new StringJoiner(".");
    int arrayCount = 0;
    for (ResponseObject parent : parents) {
      if (ARRAY_TYPE.equals(parent.getSummary()
          .getType())) {
        arrayCount++;
      } else {
        StringBuilder builder = new StringBuilder(parent.getIdentifier());
        if (parent.getSummary()
            .isEnvelope()) {
          builder.insert(0, "<")
              .append(">");
        }
        if (arrayCount > 0) {
          IntStream.range(0, arrayCount)
              .forEach(index -> builder.append("[]"));
          arrayCount = 0;
        }
        joiner.add(builder.toString());
      }
    }
    joiner.add(identifier);
    return joiner.toString();
  }
}
