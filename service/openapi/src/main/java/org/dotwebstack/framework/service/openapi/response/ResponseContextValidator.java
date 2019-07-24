package org.dotwebstack.framework.service.openapi.response;

import com.google.common.collect.ImmutableList;
import graphql.Scalars;
import java.util.List;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlField;

public class ResponseContextValidator {

  private ResponseContextValidator() {}

  public static void validate(ResponseContext responseContext) {
    GraphQlField field = responseContext.getGraphQlField();
    ResponseTemplate okResponse = responseContext.getResponses()
        .stream()
        .filter(r -> r.isApplicable(200, 299))
        .findFirst()
        .orElseThrow(() -> ExceptionHelper.unsupportedOperationException("No response in the 200 range found."));

    validate(okResponse.getResponseObject(), field);
  }

  private static void validate(ResponseFieldTemplate template, GraphQlField field) {

    String graphQlType = field.getType();
    String oasType = template.getType();
    switch (oasType) {
      case "array":
        ResponseFieldTemplate fieldTemplate = template.getItems()
            .get(0);
        validate(fieldTemplate, field);
        break;
      case "object":
        List<ResponseFieldTemplate> children = template.getChildren();
        children.forEach(child -> {
          GraphQlField graphQlChildField = field.getFields()
              .stream()
              .filter(childField -> childField.getName()
                  .equals(child.getIdentifier()))
              .findFirst()
              .orElseThrow(() -> ExceptionHelper.invalidConfigurationException(
                  "OAS field '{}' not found in matching GraphQl object '{}'", child.getIdentifier(), field.getName()));
          validate(child, graphQlChildField);
        });
        break;
      case "string":
        break;
      case "number":
        if (!ImmutableList.of(Scalars.GraphQLFloat.getName(), Scalars.GraphQLInt.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException("OAS type '{}' is not compatible with GraphQl type '{}'",
              oasType, graphQlType);
        }
        break;
      case "integer":
        if (!"Int".equals(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException("OAS type '{}' is not compatible with GraphQl type '{}'",
              oasType, graphQlType);
        }
        break;
      case "boolean":
        if (!"Boolean".equals(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException("OAS type '{}' is not compatible with GraphQl type '{}'",
              oasType, graphQlType);
        }
        break;
      default:
        throw ExceptionHelper.invalidConfigurationException("OAS type '{}' is currently not supported.", oasType);
    }
  }
}
