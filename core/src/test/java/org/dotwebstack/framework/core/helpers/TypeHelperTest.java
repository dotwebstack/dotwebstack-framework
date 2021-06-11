package org.dotwebstack.framework.core.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import graphql.Scalars;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.Coercing;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import java.util.Objects;
import java.util.stream.Stream;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("rawtypes")
class TypeHelperTest {

  @Test
  void createNonNullType_returns_typeForScalar() {
    // Arrange / Act
    NonNullType type = TypeHelper.createNonNullType(Scalars.GraphQLString);

    // Assert
    assertEquals(Scalars.GraphQLString.getName(), TypeHelper.getTypeName(type));
  }

  @Test
  void unwrapNonNullType_returnsExpectedType_forNonNullType() {
    // Arrange / Act
    TypeName type = TypeName.newTypeName(Scalars.GraphQLInt.getName())
        .build();
    Type nonNullType = NonNullType.newNonNullType(type)
        .build();

    // Assert
    assertEquals(type, TypeHelper.unwrapNonNullType(nonNullType));
  }

  @Test
  void unwrapNonNullType_returnsExpectedType_forNullType() {
    // Arrange / Act
    Type type = TypeName.newTypeName(Scalars.GraphQLInt.getName())
        .build();

    // Assert
    assertEquals(type, TypeHelper.unwrapNonNullType(type));
  }

  @ParameterizedTest()
  @MethodSource("unwrapTypeArguments")
  void unwrapType_returnsExpectedType(Type expectedType, Type inputType) {
    // Act / Assert
    assertEquals(expectedType, TypeHelper.unwrapType(inputType));
  }

  @ParameterizedTest()
  @MethodSource("getBaseTypeArguments")
  void getBaseType_returnsExpectedType(Type expectedType, Type inputType) {
    // Act / Assert
    assertEquals(expectedType, TypeHelper.getBaseType(inputType));
  }

  @ParameterizedTest()
  @MethodSource("getTypeStringArguments")
  void getBaseType_returnsExpectedType(String expectedString, Type inputType) {
    // Act / Assert
    assertEquals(expectedString, TypeHelper.getTypeString(inputType));
  }

  @ParameterizedTest()
  @MethodSource("getTypeNameGraphQlTypeArguments")
  void getTypeNameGraphQlType_returnsExpectedName(String expectedName, GraphQLType inputType, Class<Exception> clazz) {
    // Act / Assert
    if (Objects.nonNull(clazz)) {
      assertThrows(clazz, () -> TypeHelper.getTypeName(inputType));
    } else {
      assertEquals(expectedName, TypeHelper.getTypeName(inputType));
    }
  }

  @ParameterizedTest()
  @MethodSource("getTypeNameArguments")
  void getTypeNameGraphQlType_returnsExpectedName(String expectedName, Type inputType, Class<Exception> clazz) {
    // Act / Assert
    if (Objects.nonNull(clazz)) {
      assertThrows(clazz, () -> TypeHelper.hasListType(inputType));
    } else {
      assertEquals(expectedName, TypeHelper.getTypeName(inputType));
    }
  }

  @ParameterizedTest()
  @MethodSource("hasListTypeArguments")
  void getTypeNameGraphQlType_returnsExpectedName(boolean expected, Type inputType, Class<Exception> clazz) {
    // Act / Assert
    if (Objects.nonNull(clazz)) {
      assertThrows(clazz, () -> TypeHelper.hasListType(inputType));
    } else {
      assertEquals(expected, TypeHelper.hasListType(inputType));
    }
  }

  @Test
  void isNumericType_returnsFalse_forNullValue() {
    assertThat(TypeHelper.isNumericType(null), CoreMatchers.equalTo(Boolean.FALSE));
  }

  @Test
  void isNumericType_returnsTrue_forFloat() {
    assertThat(TypeHelper.isNumericType(Scalars.GraphQLFloat.getName()), CoreMatchers.equalTo(Boolean.TRUE));
  }

  @Test
  void isNumericType_returnsFalse_forString() {
    assertThat(TypeHelper.isNumericType(Scalars.GraphQLString.getName()), CoreMatchers.equalTo(Boolean.FALSE));
  }

  @Test
  void isTextType_returnsFalse_forFloat() {
    assertThat(TypeHelper.isTextType(Scalars.GraphQLFloat.getName()), CoreMatchers.equalTo(Boolean.FALSE));
  }

  @Test
  void isTextType_returnsTrue_forString() {
    assertThat(TypeHelper.isTextType(Scalars.GraphQLString.getName()), CoreMatchers.equalTo(Boolean.TRUE));
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

  private static Stream<Arguments> hasListTypeArguments() {
    TypeName intType = TypeName.newTypeName(Scalars.GraphQLInt.getName())
        .build();
    Type listType = ListType.newListType(intType)
        .build();
    Type nonNullType = NonNullType.newNonNullType(intType)
        .build();

    return Stream.of(Arguments.of(false, nonNullType, null), Arguments.of(true, listType, null),
        Arguments.of(false, intType, null), Arguments.of(false, mock(Type.class), IllegalArgumentException.class),
        Arguments.of(false, null, NullPointerException.class));
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

  private static Stream<Arguments> getTypeNameArguments() {
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

    Type typeName = TypeName.newTypeName("typename")
        .build();

    return Stream.of(Arguments.of(intType.getName(), intType, null), Arguments.of(intType.getName(), listType, null),
        Arguments.of(intType.getName(), nonNullType, null), Arguments.of(floatType.getName(), listNonNullType, null),
        Arguments.of("typename", typeName, null), Arguments.of(null, mock(Type.class), IllegalArgumentException.class),
        Arguments.of(null, null, NullPointerException.class));
  }

  private static Stream<Arguments> getTypeNameGraphQlTypeArguments() {
    GraphQLList list = GraphQLList.list(Scalars.GraphQLBoolean);
    GraphQLNonNull nonNull = GraphQLNonNull.nonNull(Scalars.GraphQLFloat);

    return Stream.of(Arguments.of(Scalars.GraphQLBoolean.getName(), list, null),
        Arguments.of(Scalars.GraphQLFloat.getName(), nonNull, null),
        Arguments.of("GraphQLObjectType", GraphQLObjectType.newObject()
            .name("GraphQLObjectType")
            .build(), null),
        Arguments.of("GraphQLInputObjectType", GraphQLInputObjectType.newInputObject()
            .name("GraphQLInputObjectType")
            .build(), null),
        Arguments.of("GraphQLScalarType", GraphQLScalarType.newScalar()
            .name("GraphQLScalarType")
            .coercing(mock(Coercing.class))
            .build(), null),
        Arguments.of(Scalars.GraphQLString.getName(), Scalars.GraphQLString, null),
        Arguments.of("Int", GraphQLTypeReference.typeRef("Int"), null),
        Arguments.of(null, mock(GraphQLType.class), IllegalArgumentException.class),
        Arguments.of(null, null, NullPointerException.class));
  }
}
