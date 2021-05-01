package org.dotwebstack.framework.core.config;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;

public final class TypeUtils {

  private TypeUtils() {}

  public static Type<TypeName> newType(String name) {
    return TypeName.newTypeName(name)
        .build();
  }

  public static Type<ListType> newListType(String name) {
    return ListType.newListType(newNonNullableType(name))
        .build();
  }

  public static Type<NonNullType> newNonNullableType(String name) {
    return NonNullType.newNonNullType(newType(name))
        .build();
  }

  public static Type<NonNullType> newNonNullableListType(String name) {
    return NonNullType.newNonNullType(newListType(name))
        .build();
  }
}
