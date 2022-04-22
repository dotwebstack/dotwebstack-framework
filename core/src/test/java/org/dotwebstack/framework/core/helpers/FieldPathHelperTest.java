package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.FieldPathHelper.createFieldPath;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.fieldPathContainsRef;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.getFieldKey;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.getLeaf;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.getObjectType;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.getParentOfRefField;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.isNested;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.isNestedFieldPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.Test;

class FieldPathHelperTest {

  @Test
  void createObjectFieldPath_returnsListOfObjectField_forPath() {
    var objectTypeFoo = createFooObjectType();

    var path = "bar.baz";

    List<ObjectField> result = createFieldPath(objectTypeFoo, path);

    assertThat(result.size(), is(2));
    assertThat(result.get(0)
        .getName(), is("bar"));
    assertThat(result.get(1)
        .getName(), is("baz"));
  }

  @Test
  void createObjectFieldPath_returnsListOfObjectField_forPathWithRef() {
    var objectTypeFoo = createFooObjectType();

    var path = "bar.ref.identifier";

    List<ObjectField> result = createFieldPath(objectTypeFoo, path);

    assertThat(result.size(), is(3));
    assertThat(result.get(0)
        .getName(), is("bar"));
    assertThat(result.get(1)
        .getName(), is("ref"));
    assertThat(result.get(2)
        .getName(), is("identifier"));
  }

  @Test
  void isNestedFieldPath_returnsTrue_forNestedFieldPath() {
    assertThat(isNestedFieldPath("test.character"), is(true));
  }

  @Test
  void isNestedFieldPath_returnsFalse_forNonNestedFieldPath() {
    assertThat(isNestedFieldPath("test"), is(false));
  }

  @Test
  void getParentOfRefField_returnsParent_forFieldPathWithRef() {
    var objectTypeFoo = createFooObjectType();
    var path = "bar.ref.identifier";

    List<ObjectField> fieldPath = createFieldPath(objectTypeFoo, path);

    var result = getParentOfRefField(fieldPath);
    assertThat(result.isPresent(), is(true));
    assertThat(result.get()
        .getName(), is("bar"));
  }

  @Test
  void getParentOfRefField_returnsEmpty_forFieldPathWithoutRef() {
    var objectTypeFoo = createFooObjectType();
    var path = "bar.baz";

    List<ObjectField> fieldPath = createFieldPath(objectTypeFoo, path);

    var result = getParentOfRefField(fieldPath);
    assertThat(result.isEmpty(), is(true));
  }

  @Test
  void getObjectType_returnsObjectType_forNestedKeyPath() {
    var objectTypeFoo = createFooObjectType();
    var path = "bar.ref.identifier";

    var result = getObjectType(objectTypeFoo, path);

    assertThat(result.getName(), is("Ref"));
  }

  @Test
  void getObjectType_returnsObjectType_forSingleKeyPath() {
    var objectTypeFoo = createFooObjectType();
    var path = "bar";

    var result = getObjectType(objectTypeFoo, path);

    assertThat(result.getName(), is("Foo"));
  }

  @Test
  void getFieldKey_returnsLastKey_forNestedKeyPath() {
    var path = "bar.ref.identifier";

    var result = getFieldKey(path);

    assertThat(result, is("identifier"));
  }

  @Test
  void getFieldKey_returnsLastKey_forSingleKeyPath() {
    var path = "identifier";

    var result = getFieldKey(path);

    assertThat(result, is("identifier"));
  }

  @Test
  void getLeaf_returnsLeaf_forFieldPath() {
    var objectTypeFoo = createFooObjectType();
    var path = "bar.ref.identifier";

    List<ObjectField> fieldPath = createFieldPath(objectTypeFoo, path);

    var result = getLeaf(fieldPath);

    assertThat(result.getName(), is("identifier"));
  }

  @Test
  void isNested_returnsTrue_forNestedLeaf() {
    var objectTypeFoo = createFooObjectType();
    var path = "bar.ref.identifier";

    List<ObjectField> fieldPath = createFieldPath(objectTypeFoo, path);

    var result = isNested(fieldPath);

    assertThat(result, is(true));
  }

  @Test
  void isNested_returnsFalse_forObjectTypeWithTable() {
    var objectTypeFoo = createFooObjectType();
    var path = "bar";

    List<ObjectField> fieldPath = createFieldPath(objectTypeFoo, path);

    var result = isNested(fieldPath);

    assertThat(result, is(false));
  }

  @Test
  void fieldPathContainsRef_returnTrue_forFieldPathWithRef() {
    var objectTypeFoo = createFooObjectType();
    var path = "bar.ref.identifier";
    List<ObjectField> fieldPath = createFieldPath(objectTypeFoo, path);

    var result = fieldPathContainsRef(fieldPath);

    assertThat(result, is(true));
  }

  @Test
  void fieldPathContainsRef_returnFalse_forFieldPathWithoutRef() {
    var objectTypeFoo = createFooObjectType();
    var path = "bar.baz";
    List<ObjectField> fieldPath = createFieldPath(objectTypeFoo, path);

    var result = fieldPathContainsRef(fieldPath);

    assertThat(result, is(false));
  }

  private TestObjectType createFooObjectType() {
    TestObjectType objectTypeFoo = new TestObjectType();
    objectTypeFoo.setName("Foo");
    objectTypeFoo.setTable("Foo");

    TestObjectField objectFieldBar = new TestObjectField();
    objectFieldBar.setObjectType(objectTypeFoo);
    objectFieldBar.setType("Bar");
    objectFieldBar.setName("bar");
    objectTypeFoo.setFields(Map.of("bar", objectFieldBar));

    TestObjectType objectTypeBar = createBarObjectType();
    objectTypeFoo.getField("bar")
        .setTargetType(objectTypeBar);

    TestObjectType objectTypeRef = createRefObjectType();
    objectTypeBar.getField("ref")
        .setTargetType(objectTypeRef);
    return objectTypeFoo;
  }

  private TestObjectType createBarObjectType() {
    TestObjectType objectTypeBar = new TestObjectType();
    objectTypeBar.setName("Bar");
    TestObjectField objectFieldBaz = new TestObjectField();
    objectFieldBaz.setObjectType(objectTypeBar);
    objectFieldBaz.setType("Baz");
    objectFieldBaz.setName("baz");
    TestObjectField objectFieldRef = new TestObjectField();
    objectFieldRef.setObjectType(objectTypeBar);
    objectFieldRef.setType("Ref");
    objectFieldRef.setName("ref");
    objectTypeBar.setFields(Map.of("baz", objectFieldBaz, "ref", objectFieldRef));
    return objectTypeBar;
  }

  private TestObjectType createRefObjectType() {
    TestObjectType objectTypeRef = new TestObjectType();
    objectTypeRef.setName("Ref");
    TestObjectField objectFieldId = new TestObjectField();
    objectFieldId.setObjectType(objectTypeRef);
    objectFieldId.setType("String");
    objectFieldId.setName("identifier");
    objectTypeRef.setFields(Map.of("identifier", objectFieldId));
    return objectTypeRef;
  }


}
