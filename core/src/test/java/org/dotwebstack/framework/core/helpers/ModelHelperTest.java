package org.dotwebstack.framework.core.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.Test;

class ModelHelperTest {

  @Test
  void createObjectFieldPath_returnsListOfObjectField_forPath() {
    Schema schema = new Schema();

    TestObjectType objectTypeFoo = createFooObjectType();
    TestObjectType objectTypeBar = createBarObjectType();
    TestObjectType objectTypeBaz = createBazObjectType();

    schema.setObjectTypes(Map.of("Foo", objectTypeFoo, "Bar", objectTypeBar, "Baz", objectTypeBaz));

    var path = "bar.baz";

    List<ObjectField> result = ModelHelper.createObjectFieldPath(schema, objectTypeFoo, path);

    assertThat(result.size(), is(2));
    assertThat(result.get(0)
        .getName(), is("bar"));
    assertThat(result.get(1)
        .getName(), is("baz"));
  }


  private TestObjectType createFooObjectType() {
    TestObjectType objectTypeFoo = new TestObjectType();
    TestObjectField objectFieldBar = new TestObjectField();
    objectFieldBar.setType("Bar");
    objectFieldBar.setName("bar");
    objectTypeFoo.setFields(Map.of("bar", objectFieldBar));
    return objectTypeFoo;
  }

  private TestObjectType createBarObjectType() {
    TestObjectType objectTypeBar = new TestObjectType();
    objectTypeBar.setName("Bar");
    TestObjectField objectFieldBaz = new TestObjectField();
    objectFieldBaz.setType("Baz");
    objectFieldBaz.setName("baz");
    objectTypeBar.setFields(Map.of("baz", objectFieldBaz));
    return objectTypeBar;
  }

  private TestObjectType createBazObjectType() {
    TestObjectType objectTypeBaz = new TestObjectType();
    objectTypeBaz.setName("Bar");
    return objectTypeBaz;
  }

  @Test
  void getObjectType_returnsObjectType_forGraphQlObjectType() {
    Schema schema = new Schema();
    ObjectType<?> objectType = new TestObjectType();
    schema.setObjectTypes(Map.of("Foo", objectType));

    GraphQLType type = GraphQLObjectType.newObject()
        .name("Foo")
        .build();

    ObjectType<?> result = ModelHelper.getObjectType(schema, type);

    assertThat(result, is(objectType));
  }

  @Test
  void getObjectType_throwException_forNonGraphQlObjectType() {
    Schema schema = new Schema();
    ObjectType<?> objectType = new TestObjectType();
    schema.setObjectTypes(Map.of("Foo", objectType));

    GraphQLType type = GraphQLEnumType.newEnum()
        .name("Foo")
        .build();

    assertThrows(IllegalStateException.class, () -> ModelHelper.getObjectType(schema, type));
  }

  @Test
  void getObjectType_throwException_forNonExistingObjectType() {
    Schema schema = new Schema();
    ObjectType<?> objectType = new TestObjectType();
    schema.setObjectTypes(Map.of("Bar", objectType));

    GraphQLType type = GraphQLObjectType.newObject()
        .name("Foo")
        .build();

    assertThrows(IllegalStateException.class, () -> ModelHelper.getObjectType(schema, type));
  }

}
