package org.dotwebstack.framework.core.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.OperationDefinition;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.Coercing;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("rawtypes")
class TypeHelperTest {

  @Test
  void isSubscription_returnsTrue() {
    var operationMock = mock(OperationDefinition.class);
    when(operationMock.getOperation()).thenReturn(OperationDefinition.Operation.SUBSCRIPTION);
    assertTrue(TypeHelper.isSubscription(operationMock));
  }

  @Test
  void isSubscription_returnsFalse() {
    var operationMock = mock(OperationDefinition.class);
    when(operationMock.getOperation()).thenReturn(OperationDefinition.Operation.MUTATION);
    assertFalse(TypeHelper.isSubscription(operationMock));
  }

  @Test
  void isListType_returnsTrue() {
    assertTrue(TypeHelper.isListType(mock(GraphQLList.class)));
  }

  @Test
  void isListType_returnsFalse() {
    assertFalse(TypeHelper.isListType(mock(GraphQLObjectType.class)));
  }

  @Test
  void unwrapConnectionType_returnsArgumentObject() {
    var nonNullType = mock(GraphQLNonNull.class);
    var objectType = mock(GraphQLObjectType.class);
    when(objectType.getName()).thenReturn("AA");
    when(nonNullType.getWrappedType()).thenReturn(objectType);

    var result = TypeHelper.unwrapConnectionType(nonNullType);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof GraphQLNonNull);
  }

  @Test
  void unwrapConnectionType_returnsUnwrappedObject() {
    var nonNullType = mock(GraphQLNonNull.class);
    var objectType = mock(GraphQLObjectType.class);
    when(objectType.getName()).thenReturn("AA Connection");
    var fieldDef = mock(GraphQLFieldDefinition.class);
    var resultMock = mock(GraphQLEnumType.class);
    when(resultMock.getName()).thenReturn("@@@@@");
    when(fieldDef.getType()).thenReturn(resultMock);
    when(objectType.getFieldDefinition("nodes")).thenReturn(fieldDef);

    when(nonNullType.getWrappedType()).thenReturn(objectType);

    var result = TypeHelper.unwrapConnectionType(nonNullType);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof GraphQLEnumType);
    assertThat(((GraphQLEnumType) result).getName(), is("@@@@@"));

  }

  @ParameterizedTest()
  @MethodSource("getBaseTypeArguments")
  void getBaseType_returnsExpectedType(Type expectedType, Type inputType) {
    // Act / Assert
    assertEquals(expectedType, TypeHelper.getBaseType(inputType));
  }

  @ParameterizedTest()
  @MethodSource("getTypeNameGraphQlTypeArguments")
  void getTypeNameGraphQlType_returnsExpectedName(
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> expectedName, GraphQLType inputType) {
    // Act / Assert
    assertThat(expectedName, equalTo(TypeHelper.getTypeName(inputType)));
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

    return Stream.of(Arguments.of(Optional.of(Scalars.GraphQLBoolean.getName()), list),
        Arguments.of(Optional.of(Scalars.GraphQLFloat.getName()), nonNull),
        Arguments.of(Optional.of("GraphQLObjectType"), GraphQLObjectType.newObject()
            .name("GraphQLObjectType")
            .build()),
        Arguments.of(Optional.of("GraphQLInputObjectType"), GraphQLInputObjectType.newInputObject()
            .name("GraphQLInputObjectType")
            .build()),
        Arguments.of(Optional.of("GraphQLScalarType"), GraphQLScalarType.newScalar()
            .name("GraphQLScalarType")
            .coercing(mock(Coercing.class))
            .build()),
        Arguments.of(Optional.of(Scalars.GraphQLString.getName()), Scalars.GraphQLString),
        Arguments.of(Optional.of("Int"), GraphQLTypeReference.typeRef("Int")),
        Arguments.of(Optional.empty(), mock(GraphQLType.class)), Arguments.of(Optional.empty(), null));
  }
}
