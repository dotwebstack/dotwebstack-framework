package org.dotwebstack.framework.core.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.idl.TypeUtil;
import org.dotwebstack.framework.core.model.AbstractObjectType;
import org.dotwebstack.framework.core.model.ObjectField;
import org.junit.jupiter.api.Test;

class TypeUtilsTest {

  private static final String TYPE_NAME = "Brewery";

  @Test
  void newType_ReturnsTypeName_Always() {
    var type = TypeUtils.newType(TYPE_NAME);

    assertTypeName(type);
  }

  @Test
  void newListType_ReturnsWrappedType_Always() {
    var type = TypeUtils.newListType(TYPE_NAME);

    assertListType(type);
  }

  @Test
  void newNonNullableType_ReturnsWrappedType_Always() {
    var type = TypeUtils.newNonNullableType(TYPE_NAME);

    assertThat(type, instanceOf(NonNullType.class));
    assertTypeName(TypeUtil.unwrapOne(type));
  }

  @Test
  void newNonNullableListType_ReturnsWrappedType_Always() {
    var type = TypeUtils.newNonNullableListType(TYPE_NAME);

    assertThat(type, instanceOf(NonNullType.class));
    assertListType(TypeUtil.unwrapOne(type));
  }

  @Test
  void createType_returnsNewType_for_ObjectFieldIsNotList_and_isNullable() {
    var objectField = mock(ObjectField.class);
    doReturn(TYPE_NAME).when(objectField)
        .getType();
    doReturn(false).when(objectField)
        .isList();
    doReturn(true).when(objectField)
        .isNullable();

    var result = TypeUtils.createType(objectField);
    assertThat(result, instanceOf(TypeName.class));
    assertTypeName(result);
  }

  @Test
  void createType_returnsNewType_for_ObjectFieldIsNotList_and_isNotNullable() {
    var objectField = mock(ObjectField.class);
    doReturn(TYPE_NAME).when(objectField)
        .getType();
    doReturn(false).when(objectField)
        .isList();
    doReturn(false).when(objectField)
        .isNullable();

    var result = TypeUtils.createType(objectField);
    assertThat(result, instanceOf(NonNullType.class));
  }

  @Test
  void createType_returnsNewType_for_ObjectFieldIsList_and_isNullable() {
    var objectField = mock(ObjectField.class);
    doReturn(TYPE_NAME).when(objectField)
        .getType();
    doReturn(true).when(objectField)
        .isList();
    doReturn(true).when(objectField)
        .isNullable();

    var result = TypeUtils.createType(objectField);
    assertThat(result, instanceOf(ListType.class));
  }

  @Test
  void createType_returnsNewType_for_ObjectFieldIsList_and_isNotNullable() {
    var objectField = mock(ObjectField.class);
    doReturn(TYPE_NAME).when(objectField)
        .getType();
    doReturn(true).when(objectField)
        .isList();
    doReturn(false).when(objectField)
        .isNullable();

    var result = TypeUtils.createType(objectField);
    assertThat(result, instanceOf(NonNullType.class));
  }

  @Test
  void createType_throwsException_forNonExistingField() {
    var objectType = new AbstractObjectType<>() {};
    objectType.setName(TYPE_NAME);

    assertThrows(IllegalArgumentException.class, () -> TypeUtils.createType("foo", objectType),
        "Field 'foo' does not exist in object type 'Brewery'.");
  }

  private static void assertTypeName(Type<?> type) {
    assertThat(type, instanceOf(TypeName.class));
    assertThat(((TypeName) type).getName(), is(equalTo(TYPE_NAME)));
  }

  private static void assertListType(Type<?> type) {
    assertThat(type, instanceOf(ListType.class));
    assertThat(TypeUtil.unwrapOne(type), instanceOf(NonNullType.class));
    assertTypeName(TypeUtil.unwrapOne(TypeUtil.unwrapOne(type)));
  }
}
