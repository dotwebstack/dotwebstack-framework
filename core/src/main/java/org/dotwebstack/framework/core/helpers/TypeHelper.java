package org.dotwebstack.framework.core.helpers;

import static graphql.language.OperationDefinition.Operation.SUBSCRIPTION;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.UNSUPPORTED_TYPE_ERROR_TEXT;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.OperationDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.Optional;
import lombok.NonNull;

@SuppressWarnings("rawtypes")
public class TypeHelper {

  private TypeHelper() {}

  public static boolean isSubscription(OperationDefinition operation) {
    return SUBSCRIPTION.equals(operation.getOperation());
  }

  public static boolean isListType(GraphQLType type) {
    return GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(type));
  }

  public static boolean hasListType(@NonNull Type<?> type) {
    if (type instanceof NonNullType) {
      return hasListType(((NonNullType) type).getType());
    } else if (type instanceof ListType) {
      return true;
    } else if (type instanceof TypeName) {
      return false;
    } else {
      throw new IllegalArgumentException(String.format(UNSUPPORTED_TYPE_ERROR_TEXT, type.getClass()));
    }
  }

  public static GraphQLType unwrapConnectionType(GraphQLType type) {
    if (type instanceof GraphQLNonNull && isConnectionType(((GraphQLNonNull) type).getWrappedType())) {
      return unwrapConnectionType(((GraphQLNonNull) type).getWrappedType());
    }
    if (isConnectionType(type)) {
      return ((GraphQLObjectType) type).getFieldDefinition("nodes")
          .getType();
    }
    return type;
  }

  private static boolean isConnectionType(GraphQLType type) {
    return type instanceof GraphQLObjectType && ((GraphQLObjectType) type).getName()
        .endsWith("Connection");
  }

  public static Type getBaseType(@NonNull Type<?> type) {
    if (type instanceof ListType) {
      return getBaseType((Type) type.getChildren()
          .get(0));
    }
    if (type instanceof NonNullType) {
      return getBaseType(((NonNullType) type).getType());
    }
    return type;
  }

  public static String getTypeName(@NonNull Type<?> type) {
    if (type instanceof NonNullType) {
      return getTypeName(((NonNullType) type).getType());
    } else if (type instanceof ListType) {
      return getTypeName(((ListType) type).getType());
    } else if (type instanceof TypeName) {
      return ((TypeName) type).getName();
    } else {
      throw new IllegalArgumentException(String.format(UNSUPPORTED_TYPE_ERROR_TEXT, type.getClass()));
    }
  }

  public static Optional<String> getTypeName(GraphQLType type) {
    if (type instanceof GraphQLList) {
      return getTypeName(((GraphQLList) type).getWrappedType());
    } else if (type instanceof GraphQLNonNull) {
      return getTypeName(((GraphQLNonNull) type).getWrappedType());
    } else if (type instanceof GraphQLNamedType) {
      return Optional.of(((GraphQLNamedType) type).getName());
    } else {
      return Optional.empty();
    }
  }
}
