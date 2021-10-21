package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isIntrospectionField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectListField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isScalarField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.TypeName;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GraphQlHelperTest {

  @Test
  void getStringValue_returnsString_forStringValue() {
    StringValue value = StringValue.newStringValue("string value")
        .build();

    String actual = GraphQlHelper.getStringValue(value);

    assertEquals("string value", actual);
  }

  @Test
  void getStringValue_returnsString_forIntValue() {
    IntValue value = IntValue.newIntValue()
        .value(BigInteger.TEN)
        .build();

    String actual = GraphQlHelper.getStringValue(value);

    assertEquals("10", actual);
  }

  @Test
  void getStringValue_returnsString_forFloatValue() {
    FloatValue value = FloatValue.newFloatValue()
        .value(BigDecimal.valueOf(10.12))
        .build();

    String actual = GraphQlHelper.getStringValue(value);

    assertEquals("10.12", actual);
  }

  @Test
  void getStringValue_returnsString_forBooleanValue() {
    BooleanValue value = BooleanValue.newBooleanValue(false)
        .build();

    String actual = GraphQlHelper.getStringValue(value);

    assertEquals("false", actual);
  }

  @Test
  void getStringValue_returnsString_forOtherValue() {
    ObjectValue value = ObjectValue.newObjectValue()
        .objectField(ObjectField.newObjectField()
            .name("field")
            .value(StringValue.newStringValue("test")
                .build())
            .build())
        .build();

    String actual = GraphQlHelper.getStringValue(value);

    assertEquals("ObjectValue{objectFields=[ObjectField{name='field', value=StringValue{value='test'}}]}", actual);
  }

  @Test
  void getValue_returnsString_forStringValue() {
    StringValue value = StringValue.newStringValue("string value")
        .build();

    String actual = (String) GraphQlHelper.getValue(TypeName.newTypeName("String")
        .build(), value);

    assertEquals("string value", actual);
  }

  @Test
  void getValue_returnsDate_forNowStringValue() {
    LocalDate localDateBeforeTest = LocalDate.now();
    StringValue value = StringValue.newStringValue("NOW")
        .build();

    Object actual = GraphQlHelper.getValue(TypeName.newTypeName("Date")
        .build(), value);

    assertEquals("LocalDate", actual.getClass()
        .getSimpleName());
    assertEquals(localDateBeforeTest, actual);
    assertEquals(LocalDate.now(), actual);
  }

  @Test
  void isScalarField_returnsFalse_forGraphQlObjectType() {
    ObjectTypeDefinition definitionMock = mock(ObjectTypeDefinition.class);
    when(definitionMock.getAdditionalData()).thenReturn(Map.of("a", "b"));

    GraphQLUnmodifiedType objectMock = mock(GraphQLObjectType.class);
    when(objectMock.getDefinition()).thenReturn(definitionMock);

    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getType()).thenReturn((GraphQLOutputType) objectMock);

    var r = isScalarField.test(selectedFieldMock);
    assertThat(r, is(false));
  }

  @Test
  void isScalarField_returnsTrue_forGraphQlScalarType() {
    GraphQLScalarType objectMock = mock(GraphQLScalarType.class);
    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getType()).thenReturn(objectMock);

    var r = isScalarField.test(selectedFieldMock);
    assertThat(r, is(true));
  }

  @Test
  void isScalarField_returnsTrue_forContainsKeyIsScalar() {
    GraphQLUnmodifiedType objectMock = mock(GraphQLObjectType.class);
    ObjectTypeDefinition definitionMock = mock(ObjectTypeDefinition.class);
    when(definitionMock.getAdditionalData()).thenReturn(Map.of("isScalar", "b"));
    when(objectMock.getDefinition()).thenReturn(definitionMock);

    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getType()).thenReturn((GraphQLOutputType) objectMock);

    var r = isScalarField.test(selectedFieldMock);
    assertThat(r, is(true));
  }

  @Test
  @Disabled
  void isObjectField_returnsTrue_forGraphQlObjectType() {
    GraphQLUnmodifiedType objectMock = mock(GraphQLObjectType.class);
    ObjectTypeDefinition definitionMock = mock(ObjectTypeDefinition.class);
    when(definitionMock.getAdditionalData()).thenReturn(Map.of("a", "b"));
    when(objectMock.getDefinition()).thenReturn(definitionMock);

    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getType()).thenReturn((GraphQLOutputType) objectMock);

    var r = isObjectField.test(selectedFieldMock);
    assertThat(r, is(true));
  }

  @Test
  void isObjectField_returnsFalse_forIsConnectionType() {
    ObjectTypeDefinition definitionMock = mock(ObjectTypeDefinition.class);
    Map<String, String> additionalData = new HashMap<>();
    additionalData.put("isConnectionType", "b");
    when(definitionMock.getAdditionalData()).thenReturn(additionalData);

    GraphQLUnmodifiedType objectMock = mock(GraphQLObjectType.class);
    when(objectMock.getDefinition()).thenReturn(definitionMock);

    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getType()).thenReturn((GraphQLOutputType) objectMock);

    var r = isObjectField.test(selectedFieldMock);
    assertThat(r, is(false));
  }

  @Test
  void isObjectListField_returnsTrue_forIsConnectionType() {
    ObjectTypeDefinition definitionMock = mock(ObjectTypeDefinition.class);
    Map<String, String> additionalData = new HashMap<>();
    additionalData.put("isConnectionType", "b");
    when(definitionMock.getAdditionalData()).thenReturn(additionalData);

    GraphQLUnmodifiedType objectMock = mock(GraphQLObjectType.class);
    when(objectMock.getDefinition()).thenReturn(definitionMock);

    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getType()).thenReturn((GraphQLOutputType) objectMock);

    var r = isObjectListField.test(selectedFieldMock);
    assertThat(r, is(true));
  }

  @Test
  void isObjectListField_returnsFalse_forNotList_andNotIsConnectionType() {
    ObjectTypeDefinition definitionMock = mock(ObjectTypeDefinition.class);
    Map<String, String> additionalData = new HashMap<>();
    additionalData.put("a", "b");
    when(definitionMock.getAdditionalData()).thenReturn(additionalData);

    GraphQLUnmodifiedType objectMock = mock(GraphQLObjectType.class);
    when(objectMock.getDefinition()).thenReturn(definitionMock);

    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getType()).thenReturn((GraphQLOutputType) objectMock);

    var r = isObjectListField.test(selectedFieldMock);
    assertThat(r, is(false));
  }

  @Test
  void isIntrospectionField_returnsTrue_forRightName() {
    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getName()).thenReturn("__AA");

    var r = isIntrospectionField.test(selectedFieldMock);
    assertThat(r, is(true));
  }

  @Test
  void isIntrospectionField_returnsFalse_forOtherName() {
    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getName()).thenReturn("AA");

    var r = isIntrospectionField.test(selectedFieldMock);
    assertThat(r, is(false));
  }

}
