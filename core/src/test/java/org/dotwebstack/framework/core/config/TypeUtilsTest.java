package org.dotwebstack.framework.core.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.schema.idl.TypeUtil;
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
