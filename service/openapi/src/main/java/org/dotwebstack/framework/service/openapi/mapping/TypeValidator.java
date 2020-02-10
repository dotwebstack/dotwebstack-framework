package org.dotwebstack.framework.service.openapi.mapping;

import static graphql.Scalars.GraphQLBigDecimal;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLByte;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLShort;
import static graphql.Scalars.GraphQLString;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.BOOLEAN_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.INTEGER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.NUMBER_TYPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.STRING_TYPE;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;

public class TypeValidator {

  public static final String MAPPING_ERROR_TO = "OAS type '{}' in property '{}' cannot be mapped to GraphQl type '{}'.";

  public static final String MAPPING_ERROR_FROM =
      "OAS type '{}' in property '{}' cannot be mapped from GraphQl type '{}'.";

  public void validateTypesOpenApiToGraphQ(@NonNull String oasType, @NonNull String graphQlType,
      @NonNull String identifier) {
    switch (oasType) {
      case STRING_TYPE:
        if (!GraphQLID.getName()
            .equals(graphQlType)
            && !GraphQLString.getName()
                .equals(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(MAPPING_ERROR_TO, oasType, identifier, graphQlType);
        }
        break;
      case NUMBER_TYPE:
        if (!ImmutableList
            .of(GraphQLFloat.getName(), GraphQLInt.getName(), GraphQLLong.getName(), GraphQLByte.getName(),
                GraphQLShort.getName(), GraphQLBigDecimal.getName(), GraphQLString.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(MAPPING_ERROR_TO, oasType, identifier, graphQlType);
        }
        break;
      case INTEGER_TYPE:
        if (!ImmutableList
            .of(GraphQLInt.getName(), GraphQLByte.getName(), GraphQLShort.getName(), GraphQLString.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(MAPPING_ERROR_TO, oasType, identifier, graphQlType);
        }
        break;
      case BOOLEAN_TYPE:
        if (!GraphQLBoolean.getName()
            .equals(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(MAPPING_ERROR_TO, oasType, identifier, graphQlType);
        }
        break;
      default:
        throw ExceptionHelper.invalidConfigurationException("OAS type '{}' is currently not supported.", oasType);
    }
  }

  public void validateTypesGraphQlToOpenApi(@NonNull String oasType, @NonNull String graphQlType,
      @NonNull String identifier) {
    switch (oasType) {
      case STRING_TYPE:
        break;
      case NUMBER_TYPE:
        if (!ImmutableList
            .of(GraphQLFloat.getName(), GraphQLInt.getName(), GraphQLLong.getName(), GraphQLByte.getName(),
                GraphQLShort.getName(), GraphQLBigDecimal.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(MAPPING_ERROR_FROM, oasType, identifier, graphQlType);
        }
        break;
      case INTEGER_TYPE:
        if (!ImmutableList.of(GraphQLInt.getName(), GraphQLByte.getName(), GraphQLShort.getName())
            .contains(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(MAPPING_ERROR_FROM, oasType, identifier, graphQlType);
        }
        break;
      case BOOLEAN_TYPE:
        if (!GraphQLBoolean.getName()
            .equals(graphQlType)) {
          throw ExceptionHelper.invalidConfigurationException(MAPPING_ERROR_FROM, oasType, identifier, graphQlType);
        }
        break;
      default:
        throw ExceptionHelper.invalidConfigurationException("OAS type '{}' is currently not supported.", oasType);
    }
  }
}
