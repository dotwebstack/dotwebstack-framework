package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.UNSUPPORTED_TYPE_ERROR_TEXT;

import graphql.Scalars;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import java.util.List;
import lombok.NonNull;

@SuppressWarnings("rawtypes")
public class TypeHelper {
  private TypeHelper() {}

  public static boolean hasListType(@NonNull Type<?> type) {
    if (type instanceof NonNullType) {
      return hasListType(((NonNullType) type).getType());
    } else if (type instanceof ListType) {
      return true;
    } else if (type instanceof TypeName) {
      return false;
    } else {
      throw ExceptionHelper.illegalArgumentException(UNSUPPORTED_TYPE_ERROR_TEXT, type.getClass());
    }
  }

  public static Type unwrapNonNullType(@NonNull Type<?> type) {
    if (type instanceof NonNullType) {
      return (Type) type.getChildren()
          .get(0);
    }
    return type;
  }

  public static Type unwrapType(@NonNull Type<?> type) {
    if (type instanceof ListType) {
      return (Type) type.getChildren()
          .get(0);
    }
    if (type instanceof NonNullType) {
      return ((NonNullType) type).getType();
    }
    return type;
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

  public static String getTypeString(@NonNull Type<?> type) {
    var builder = new StringBuilder();
    if (type instanceof ListType) {
      builder.append("[");
      builder.append(getTypeString(unwrapType(type)));
      builder.append("]");
    } else if (type instanceof NonNullType) {
      builder.append(getTypeString(unwrapType(type)));
      builder.append("!");
    } else {
      builder.append(((TypeName) type).getName());
    }
    return builder.toString();
  }

  public static String getTypeName(@NonNull Type<?> type) {
    if (type instanceof NonNullType) {
      return getTypeName(((NonNullType) type).getType());
    } else if (type instanceof ListType) {
      return getTypeName(((ListType) type).getType());
    } else if (type instanceof TypeName) {
      return ((TypeName) type).getName();
    } else {
      throw ExceptionHelper.illegalArgumentException(UNSUPPORTED_TYPE_ERROR_TEXT, type.getClass());
    }
  }

  public static String getTypeName(@NonNull GraphQLType type) {
    if (type instanceof GraphQLList) {
      return getTypeName(((GraphQLList) type).getWrappedType());
    } else if (type instanceof GraphQLNonNull) {
      return getTypeName(((GraphQLNonNull) type).getWrappedType());
    } else if (type instanceof GraphQLNamedType) {
      return ((GraphQLNamedType) type).getName();
    } else {
      throw ExceptionHelper.illegalArgumentException(UNSUPPORTED_TYPE_ERROR_TEXT, type.getClass());
    }
  }

  public static NonNullType createNonNullType(@NonNull GraphQLType type) {
    TypeName optionalType = TypeName.newTypeName(getTypeName(type))
        .build();
    return NonNullType.newNonNullType(optionalType)
        .build();
  }

  public static boolean isNumericType(@NonNull Type<?> type) {
    List<String> numericType = List.of(Scalars.GraphQLFloat.getName(), Scalars.GraphQLInt.getName());
    return numericType.contains(getTypeName(type));
  }

  public static boolean isTextType(@NonNull Type<?> type) {
    return getTypeName(type).equals(Scalars.GraphQLString.getName());
  }
}
