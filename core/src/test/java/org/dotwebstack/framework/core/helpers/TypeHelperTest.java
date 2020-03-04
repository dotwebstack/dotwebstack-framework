package org.dotwebstack.framework.core.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import graphql.Scalars;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("rawtypes")
public class TypeHelperTest {

  @Test
  public void createNonNullType_returns_typeForScalar() {
    // Arrange / Act
    NonNullType type = TypeHelper.createNonNullType(Scalars.GraphQLString);

    // Assert
    assertEquals(Scalars.GraphQLString.getName(), TypeHelper.getTypeName(type));
  }

  @ParameterizedTest()
  @MethodSource("unwrapTypeArguments")
  public void unwrapType_returnsExpectedType(Type expectedType, Type inputType) {
    // Act / Assert
    assertEquals(expectedType, TypeHelper.unwrapType(inputType));
  }

  @ParameterizedTest()
  @MethodSource("getBaseTypeArguments")
  public void getBaseType_returnsExpectedType(Type expectedType, Type inputType) {
    // Act / Assert
    assertEquals(expectedType, TypeHelper.getBaseType(inputType));
  }

  @ParameterizedTest()
  @MethodSource("getTypeStringArguments")
  public void getBaseType_returnsExpectedType(String expectedString, Type inputType) {
    // Act / Assert
    assertEquals(expectedString, TypeHelper.getTypeString(inputType));
  }

  private static Stream<Arguments> unwrapTypeArguments() {

    TypeName intType = TypeName.newTypeName(Scalars.GraphQLInt.getName())
        .build();
    Type listType = ListType.newListType(intType)
        .build();
    Type nonNullType = NonNullType.newNonNullType(intType)
        .build();

    TypeName floatType = TypeName.newTypeName(Scalars.GraphQLFloat.getName())
        .build();
    Type nonNullFloatType = NonNullType.newNonNullType(floatType)
        .build();

    return Stream.of(Arguments.of(intType, listType), Arguments.of(intType, nonNullType),
        Arguments.of(floatType, nonNullFloatType), Arguments.of(floatType, floatType));
  }

  private static Stream<Arguments> getBaseTypeArguments() {

    TypeName intType = TypeName.newTypeName(Scalars.GraphQLInt.getName())
        .build();
    Type listType = ListType.newListType(intType)
        .build();
    Type nonNullType = NonNullType.newNonNullType(intType)
        .build();

    TypeName floatType = TypeName.newTypeName(Scalars.GraphQLFloat.getName())
        .build();
    Type nonNullFloatType = NonNullType.newNonNullType(floatType)
        .build();
    Type listNonNullType = NonNullType.newNonNullType(ListType.newListType(nonNullFloatType)
        .build())
        .build();

    return Stream.of(Arguments.of(intType, listType), Arguments.of(intType, nonNullType),
        Arguments.of(floatType, listNonNullType), Arguments.of(floatType, floatType));
  }

  private static Stream<Arguments> getTypeStringArguments() {

    TypeName intType = TypeName.newTypeName(Scalars.GraphQLInt.getName())
        .build();
    Type listType = ListType.newListType(intType)
        .build();
    Type nonNullType = NonNullType.newNonNullType(intType)
        .build();

    TypeName floatType = TypeName.newTypeName(Scalars.GraphQLFloat.getName())
        .build();
    Type nonNullFloatType = NonNullType.newNonNullType(floatType)
        .build();
    ListType floatListType = ListType.newListType(nonNullFloatType)
        .build();
    Type listNonNullType = NonNullType.newNonNullType(floatListType)
        .build();

    return Stream.of(Arguments.of("[Int]", listType), Arguments.of("Int!", nonNullType),
        Arguments.of("[Float!]", floatListType), Arguments.of("[Float!]!", listNonNullType),
        Arguments.of("Float", floatType));
  }
}
