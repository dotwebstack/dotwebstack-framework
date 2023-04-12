package org.dotwebstack.framework.core.helpers;

import static graphql.language.OperationDefinition.Operation.SUBSCRIPTION;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.UNSUPPORTED_TYPE_ERROR_TEXT;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.OperationDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import java.util.Optional;
import lombok.NonNull;

@SuppressWarnings("rawtypes")
public final class TypeHelper {

  public static final String QUERY_TYPE_NAME = "Query";

  public static final String SUBSCRIPTION_TYPE_NAME = "Subscription";

  private TypeHelper() {}

  public static boolean isSubscription(OperationDefinition operation) {
    return SUBSCRIPTION.equals(operation.getOperation());
  }

  public static boolean isSubscription(GraphQLOutputType type) {
    return getTypeName(type).filter(SUBSCRIPTION_TYPE_NAME::equals)
        .isPresent();
  }

  public static boolean isQuery(GraphQLOutputType type) {
    return getTypeName(type).filter(QUERY_TYPE_NAME::equals)
        .isPresent();
  }

  public static boolean isListType(GraphQLType type) {
    return GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(type));
  }

  public static boolean hasListType(@NonNull Type<?> type) {
    if (type instanceof NonNullType nonNullType) {
      return hasListType(nonNullType.getType());
    } else if (type instanceof ListType) {
      return true;
    } else if (type instanceof TypeName) {
      return false;
    } else {
      throw illegalArgumentException(UNSUPPORTED_TYPE_ERROR_TEXT, type.getClass());
    }
  }

  public static GraphQLType unwrapConnectionType(GraphQLType type) {
    if (type instanceof GraphQLNonNull nonNullType && isConnectionType(nonNullType.getWrappedType())) {
      return unwrapConnectionType(nonNullType.getWrappedType());
    }
    if (isConnectionType(type)) {
      return ((GraphQLObjectType) type).getFieldDefinition("nodes")
          .getType();
    }
    return type;
  }

  private static boolean isConnectionType(GraphQLType type) {
    return type instanceof GraphQLObjectType objectType && objectType.getName()
        .endsWith("Connection");
  }

  public static Type getBaseType(@NonNull Type<?> type) {
    if (type instanceof ListType) {
      return getBaseType((Type) type.getChildren()
          .get(0));
    }
    if (type instanceof NonNullType nonNullType) {
      return getBaseType(nonNullType.getType());
    }
    return type;
  }

  public static String getTypeName(@NonNull Type<?> type) {
    if (type instanceof NonNullType nonNullType) {
      return getTypeName(nonNullType.getType());
    } else if (type instanceof ListType listType) {
      return getTypeName(listType.getType());
    } else if (type instanceof TypeName typeName) {
      return typeName.getName();
    } else {
      throw illegalArgumentException(UNSUPPORTED_TYPE_ERROR_TEXT, type.getClass());
    }
  }

  public static Optional<String> getTypeName(GraphQLType type) {
    if (type instanceof GraphQLList listType) {
      return getTypeName(listType.getWrappedType());
    } else if (type instanceof GraphQLNonNull nonNullType) {
      return getTypeName(nonNullType.getWrappedType());
    } else if (type instanceof GraphQLNamedType namedType) {
      return Optional.of(namedType.getName());
    } else {
      return Optional.empty();
    }
  }
}
