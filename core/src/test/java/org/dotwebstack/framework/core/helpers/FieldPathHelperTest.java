package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.FieldPathHelper.createFieldPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.Test;

class FieldPathHelperTest {

  @Test
  void createObjectFieldPath_returnsListOfObjectField_forPath() {
    Schema schema = new Schema();

    TestObjectType objectTypeFoo = createFooObjectType();
    TestObjectType objectTypeBar = createBarObjectType();
    TestObjectType objectTypeBaz = createBazObjectType();

    objectTypeFoo.getField("bar")
        .setTargetType(objectTypeBar);

    schema.setObjectTypes(Map.of("Foo", objectTypeFoo, "Bar", objectTypeBar, "Baz", objectTypeBaz));

    var path = "bar.baz";

    List<ObjectField> result = createFieldPath(objectTypeFoo, path);

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
    objectTypeBaz.setName("Baz");
    return objectTypeBaz;
  }
}
