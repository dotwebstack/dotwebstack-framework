package org.dotwebstack.framework.service.openapi.response;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLByte;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLShort;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlField;

public class ResponseContextValidator {

  public void validate(@NonNull ResponseContext responseContext) {
    GraphQlField field = responseContext.getGraphQlField();
    ResponseTemplate okResponse = responseContext.getResponses()
        .stream()
        .filter(responseTemplate -> responseTemplate.isApplicable(200, 299))
        .findFirst()
        .orElseThrow(() -> ExceptionHelper.unsupportedOperationException("No response in the 200 range found."));

    validate(okResponse.getResponseObject(), field);
  }

  private void validate(ResponseObject template, GraphQlField field) {
    String graphQlType = field.getType();
    String oasType = template.getType();
    switch (oasType) {
      case "array":
        ResponseObject fieldTemplate = template.getItems()
            .get(0);
        validate(fieldTemplate, field);
        break;
      case "object":
        List<ResponseObject> children = template.getChildren();
        children.forEach(child -> {
          GraphQlField graphQlChildField = field.getFields()
              .stream()
              .filter(childField -> childField.getName()
                  .equals(child.getIdentifier()))
              .findFirst()
              .orElseThrow(() -> ExceptionHelper.invalidConfigurationException(
                  "OAS field '{}' not found in matching GraphQl object '{}'.", child.getIdentifier(), field.getName()));
          validate(child, graphQlChildField);
        });
        break;
      default:
        validateTypes(oasType, graphQlType, template.getIdentifier());
    }
  }

  protected void validateTypes(String oasType, String graphQlType, String identifier) {
    switch (oasType) {
      case "string":
        break;
      case "number":
        if (!ImmutableList
            .of(GraphQLFloat.getName(), GraphQLInt.getName(), GraphQLLong.getName(), GraphQLByte.getName(),
                GraphQLShort.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(
              "OAS type '{}' in property '{}' is not compatible with GraphQl type '{}'.", oasType, graphQlType,
              identifier);
        }
        break;
      case "integer":
        if (!ImmutableList.of(GraphQLInt.getName(), GraphQLByte.getName(), GraphQLShort.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(
              "OAS type '{}' in property '{}' is not compatible with GraphQl type '{}'.", oasType, graphQlType,
              identifier);
        }
        break;
      case "boolean":
        if (!GraphQLBoolean.getName()
            .equals(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(
              "OAS type '{}' in property '{}' is not compatible with GraphQl type '{}'.", oasType, graphQlType,
              identifier);
        }
        break;
      default:
        throw ExceptionHelper.invalidConfigurationException("OAS type '{}' is currently not supported.", oasType);
    }

  }
}
