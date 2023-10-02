package org.dotwebstack.framework.core.config;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import org.dotwebstack.framework.core.model.FieldArgument;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Subscription;

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

  public static Type<NonNullType> createType(String key, ObjectType<?> objectType) {
    return createType(key, objectType, "");
  }

  public static Type<NonNullType> createType(String key, ObjectType<?> objectType, String nonScalarTypePostfix) {
    var fieldConfig = objectType.getField(key);
    var type = isScalarType(fieldConfig.getType()) ? fieldConfig.getType()
        : fieldConfig.getType()
            .concat(nonScalarTypePostfix);

    return TypeUtils.newNonNullableType(type);
  }

  public static Type<NonNullType> createType(Subscription subscription) {
    return TypeUtils.newNonNullableType(subscription.getType());
  }

  public static Type<?> createType(ObjectField objectField) {
    var type = objectField.getType();

    if (objectField.isList()) {
      return objectField.isNullable() ? TypeUtils.newListType(type) : TypeUtils.newNonNullableListType(type);
    }

    return objectField.isNullable() ? TypeUtils.newType(type) : TypeUtils.newNonNullableType(type);
  }

  public static Type<?> createType(FieldArgument fieldArgument) {
    var type = fieldArgument.getType();

    if (fieldArgument.isList()) {
      return fieldArgument.isNullable() ? TypeUtils.newListType(type) : TypeUtils.newNonNullableListType(type);
    }

    return fieldArgument.isNullable() ? TypeUtils.newType(type) : TypeUtils.newNonNullableType(type);
  }

  public static boolean isScalarType(String type) {
    var result = false;
    if (GraphQLBoolean.getName()
        .equals(type)) {
      result = true;
    } else if (GraphQLInt.getName()
        .equals(type)) {
      result = true;
    } else if (GraphQLFloat.getName()
        .equals(type)) {
      result = true;
    } else if (GraphQLString.getName()
        .equals(type)) {
      result = true;
    } else if (GraphQLID.getName()
        .equals(type)) {
      result = true;
    }
    return result;
  }
}
