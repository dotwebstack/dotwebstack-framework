package org.dotwebstack.framework.service.openapi.response;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLByte;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLShort;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.ARRAY_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.BOOLEAN_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.INTEGER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.NUMBER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.OBJECT_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.STRING_TYPE;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.springframework.stereotype.Component;

@Component
public class ResponseContextValidator {

  public void validate(@NonNull ResponseObject template, @NonNull GraphQlField field) {
    String graphQlType = field.getType();
    String oasType = template.getType();
    switch (oasType) {
      case ARRAY_TYPE:
        ResponseObject fieldTemplate = template.getItems()
            .get(0);
        validate(fieldTemplate, field);
        break;
      case OBJECT_TYPE:
        List<ResponseObject> children = template.getChildren();
        children.forEach(child -> {
          if (child.isEnvelope()) {
            ResponseObject embedded = child.getChildren()
                .get(0);
            validate(embedded, field);
          } else {
            GraphQlField graphQlChildField = field.getFields()
                .stream()
                .filter(childField -> childField.getName()
                    .equals(child.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> ExceptionHelper.invalidConfigurationException(
                    "OAS field '{}' not found in matching GraphQl object '{}'.", child.getIdentifier(),
                    field.getName()));
            validate(child, graphQlChildField);
          }
        });
        break;
      default:
        validateTypes(oasType, graphQlType, template.getIdentifier());
    }
  }

  protected void validateTypes(String oasType, String graphQlType, String identifier) {
    switch (oasType) {
      case STRING_TYPE:
        break;
      case NUMBER_TYPE:
        if (!ImmutableList
            .of(GraphQLFloat.getName(), GraphQLInt.getName(), GraphQLLong.getName(), GraphQLByte.getName(),
                GraphQLShort.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(
              "OAS type '{}' in property '{}' is not compatible with GraphQl type '{}'.", oasType, graphQlType,
              identifier);
        }
        break;
      case INTEGER_TYPE:
        if (!ImmutableList.of(GraphQLInt.getName(), GraphQLByte.getName(), GraphQLShort.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(
              "OAS type '{}' in property '{}' is not compatible with GraphQl type '{}'.", oasType, graphQlType,
              identifier);
        }
        break;
      case BOOLEAN_TYPE:
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
