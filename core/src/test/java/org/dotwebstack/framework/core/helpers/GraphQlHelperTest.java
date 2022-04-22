package org.dotwebstack.framework.core.helpers;

import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLScalarType.newScalar;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getAdditionalData;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getKeyArguments;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getQueryName;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getRequestStepInfo;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isCustomValueField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isIntrospectionField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectListField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isScalarField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.execution.ExecutionStepInfo;
import graphql.language.BooleanValue;
import graphql.language.FieldDefinition;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.TypeName;
import graphql.scalar.GraphqlStringCoercing;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
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
  void isObjectField_returnsTrue_forGraphQlObjectType() {
    GraphQLUnmodifiedType objectMock = mock(GraphQLObjectType.class);
    ObjectTypeDefinition definitionMock = mock(ObjectTypeDefinition.class);
    when(definitionMock.getAdditionalData()).thenReturn(Map.of("a", "b"));
    when(objectMock.getDefinition()).thenReturn(definitionMock);
    when(objectMock.getName()).thenReturn("not Aggregate");

    SelectedField selectedFieldMock = mock(SelectedField.class);
    when(selectedFieldMock.getType()).thenReturn((GraphQLOutputType) objectMock);

    assertThat(isObjectField.test(selectedFieldMock), is(true));
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

  @Test
  void getRequestStepInfo_returnsRequestStepInfo_forNestedExceptionStepInfo() {
    ExecutionStepInfo root = mock(ExecutionStepInfo.class);

    ExecutionStepInfo request = mock(ExecutionStepInfo.class);
    when(request.getParent()).thenReturn(root);
    when(request.hasParent()).thenReturn(true);

    ExecutionStepInfo nested = mock(ExecutionStepInfo.class);
    when(nested.getParent()).thenReturn(request);
    when(nested.hasParent()).thenReturn(true);

    var actual = getRequestStepInfo(nested);

    assertThat(actual, equalTo(request));
  }

  @Test
  void getRequestStepInfo_returnsExisting_forRequestStepInfo() {
    ExecutionStepInfo root = mock(ExecutionStepInfo.class);

    ExecutionStepInfo request = mock(ExecutionStepInfo.class);
    when(request.getParent()).thenReturn(root);
    when(request.hasParent()).thenReturn(true);

    var actual = getRequestStepInfo(request);

    assertThat(actual, equalTo(request));
  }

  @Test
  void getKeyArguments_returnsList_forArgumentsWithKeyField() {
    var type = GraphQLList.list(newObject().name("Item")
        .build());

    var fieldDefinition = newFieldDefinition().name("itemsBatchQuery")
        .definition(FieldDefinition.newFieldDefinition()
            .name("itemsBatchQuery")
            .additionalData(GraphQlConstants.IS_BATCH_KEY_QUERY, Boolean.TRUE.toString())
            .build())
        .type(type)
        .argument(newArgument().type(GraphQLList.list(newScalar().name("String")
            .coercing(new GraphqlStringCoercing())
            .build()))
            .definition(newInputValueDefinition().additionalData(GraphQlConstants.KEY_FIELD, Boolean.TRUE.toString())
                .build())
            .name("identifier")
            .build())
        .build();

    var result = getKeyArguments(fieldDefinition);

    assertThat(result.size(), is(1));
    assertThat(result.get(0)
        .getName(), is("identifier"));
  }

  @Test
  void getKeyArguments_returnsEmptyList_forArgumentsWithoutKeyField() {
    var type = GraphQLList.list(newObject().name("Item")
        .build());

    var fieldDefinition = newFieldDefinition().name("itemsBatchQuery")
        .definition(FieldDefinition.newFieldDefinition()
            .name("itemsBatchQuery")
            .additionalData(GraphQlConstants.IS_BATCH_KEY_QUERY, Boolean.TRUE.toString())
            .build())
        .type(type)
        .argument(newArgument().type(GraphQLList.list(newScalar().name("String")
            .coercing(new GraphqlStringCoercing())
            .build()))
            .definition(newInputValueDefinition().additionalData(Map.of())
                .build())
            .name("identifier")
            .build())
        .build();

    var result = getKeyArguments(fieldDefinition);

    assertThat(result.size(), is(0));
  }

  @Test
  void getFieldDefinition_throwsException_forSelectedFieldWithMultipleFieldDefinitions() {
    var selectedField = mock(SelectedField.class);

    when(selectedField.getName()).thenReturn("testSelectedField");

    when(selectedField.getFieldDefinitions()).thenReturn(List.of(newFieldDefinition().name("fieldDefA")
        .definition(FieldDefinition.newFieldDefinition()
            .build())
        .type(Scalars.GraphQLString)
        .build(),
        newFieldDefinition().name("fieldDefB")
            .definition(FieldDefinition.newFieldDefinition()
                .build())
            .type(Scalars.GraphQLString)
            .build()));

    var thrown = assertThrows(IllegalArgumentException.class, () -> GraphQlHelper.getFieldDefinition(selectedField));

    assertThat(thrown.getMessage(),
        equalTo("SelectedField 'testSelectedField' has 2 fieldDefinitions but expected one!"));
  }

  @Test
  void getFieldDefinition_returnsDefinition_forSelectedFieldWithSingleFieldDefinition() {
    var selectedField = mock(SelectedField.class);

    when(selectedField.getName()).thenReturn("testSelectedField");

    var fieldDefinition = newFieldDefinition().name("fieldDefA")
        .definition(FieldDefinition.newFieldDefinition()
            .build())
        .type(Scalars.GraphQLString)
        .build();

    when(selectedField.getFieldDefinitions()).thenReturn(List.of(fieldDefinition));

    var result = GraphQlHelper.getFieldDefinition(selectedField);

    assertThat(result, equalTo(result));
  }

  @Test
  void isCustomValueField_returnsTrue_forCustomField() {
    var selectedField = mock(SelectedField.class);

    var fieldDefinition = newFieldDefinition().name("fieldDefA")
        .definition(FieldDefinition.newFieldDefinition()
            .additionalData(CUSTOM_FIELD_VALUEFETCHER, "testValuefetcher")
            .build())
        .type(Scalars.GraphQLString)
        .build();

    when(selectedField.getFieldDefinitions()).thenReturn(List.of(fieldDefinition));

    var result = isCustomValueField.test(selectedField);
    assertThat(result, is(true));
  }

  @Test
  void isCustomValueField_returnsFalse_forNonCustomField() {
    var selectedField = mock(SelectedField.class);

    var fieldDefinition = newFieldDefinition().name("fieldDefA")
        .definition(FieldDefinition.newFieldDefinition()
            .build())
        .type(Scalars.GraphQLString)
        .build();

    when(selectedField.getFieldDefinitions()).thenReturn(List.of(fieldDefinition));

    var result = isCustomValueField.test(selectedField);
    assertThat(result, is(false));
  }

  @Test
  void getAdditionalData_returnsAdditionalData_forSelectedField() {
    var selectedField = mock(SelectedField.class);

    var fieldDefinition = newFieldDefinition().name("fieldDefA")
        .definition(FieldDefinition.newFieldDefinition()
            .additionalData("test", "testvalue")
            .build())
        .type(Scalars.GraphQLString)
        .build();

    when(selectedField.getFieldDefinitions()).thenReturn(List.of(fieldDefinition));

    var result = getAdditionalData(selectedField, "test");

    assertThat(result, is(Optional.of("testvalue")));
  }

  @Test
  void getQueryName_returnsQueryName_forQuery() {
    var request = mock(ExecutionStepInfo.class);
    var objectType = mock(GraphQLObjectType.class);
    var fieldDefinition = newFieldDefinition().name("queryDefA")
        .type(GraphQLList.list(Scalars.GraphQLString))
        .build();

    when(request.getObjectType()).thenReturn(objectType);
    when(objectType.getName()).thenReturn("Query");
    when(request.getFieldDefinition()).thenReturn(fieldDefinition);

    var result = getQueryName(request);

    assertThat(result, is(Optional.of("queryDefA")));
  }

  @Test
  void getQueryName_returnsOptionalEmpty_forScalar() {
    var request = mock(ExecutionStepInfo.class);
    var objectType = mock(GraphQLObjectType.class);
    var fieldDefinition = newFieldDefinition().name("queryDefA")
        .type(Scalars.GraphQLString)
        .build();

    when(request.getObjectType()).thenReturn(objectType);
    when(objectType.getName()).thenReturn("test");
    when(request.getFieldDefinition()).thenReturn(fieldDefinition);

    var result = getQueryName(request);

    assertThat(result, is(Optional.empty()));
  }
}
