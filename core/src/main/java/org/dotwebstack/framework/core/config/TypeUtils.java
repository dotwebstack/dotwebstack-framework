package org.dotwebstack.framework.core.config;

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
    var fieldConfig = objectType.getFields()
        .get(key);
    return TypeUtils.newNonNullableType(fieldConfig.getType());
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

}
