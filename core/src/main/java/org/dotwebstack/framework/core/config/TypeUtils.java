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

  public static Type<?> createType(String key,
      AbstractTypeConfiguration<? extends AbstractFieldConfiguration> typeConfiguration) {
    AbstractFieldConfiguration fieldConfig = typeConfiguration.getFields()
        .get(key);
    return TypeUtils.newNonNullableType(fieldConfig.getType());
  }

  public static Type<?> createType(SubscriptionConfiguration subscriptionConfiguration) {
    return TypeUtils.newNonNullableType(subscriptionConfiguration.getType());
  }

  public static Type<?> createType(QueryConfiguration queryConfiguration) {
    var type = queryConfiguration.getType();

    if (queryConfiguration.isList()) {
      return queryConfiguration.isNullable() ? TypeUtils.newListType(type) : TypeUtils.newNonNullableListType(type);
    }

    return queryConfiguration.isNullable() ? TypeUtils.newType(type) : TypeUtils.newNonNullableType(type);
  }

  public static Type<?> createType(FieldConfiguration fieldConfiguration) {
    var type = fieldConfiguration.getType();

    if (fieldConfiguration.isList()) {
      return fieldConfiguration.isNullable() ? TypeUtils.newListType(type) : TypeUtils.newNonNullableListType(type);
    }

    return fieldConfiguration.isNullable() ? TypeUtils.newType(type) : TypeUtils.newNonNullableType(type);
  }

  public static Type<?> createType(FieldArgumentConfiguration fieldArgumentConfiguration) {
    var type = fieldArgumentConfiguration.getType();

    if (fieldArgumentConfiguration.isList()) {
      return fieldArgumentConfiguration.isNullable() ? TypeUtils.newListType(type)
          : TypeUtils.newNonNullableListType(type);
    }

    return fieldArgumentConfiguration.isNullable() ? TypeUtils.newType(type) : TypeUtils.newNonNullableType(type);
  }

}
