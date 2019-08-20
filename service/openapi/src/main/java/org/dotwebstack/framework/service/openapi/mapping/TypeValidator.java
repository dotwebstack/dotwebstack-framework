package org.dotwebstack.framework.service.openapi.mapping;

import static graphql.Scalars.GraphQLBigDecimal;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLByte;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLShort;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.BOOLEAN_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.INTEGER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.NUMBER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.STRING_TYPE;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;

public class TypeValidator {

  public void validateTypes(String oasType, String graphQlType, String identifier) {
    switch (oasType) {
      case STRING_TYPE:
        break;
      case NUMBER_TYPE:
        if (!ImmutableList
            .of(GraphQLFloat.getName(), GraphQLInt.getName(), GraphQLLong.getName(), GraphQLByte.getName(),
                GraphQLShort.getName(), GraphQLBigDecimal.getName())
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
