package org.dotwebstack.framework.core.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.TypeName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class GraphQlValueHelperTest {

  @Test
  void getStringValue_returnsString_forStringValue() {
    StringValue value = StringValue.newStringValue("string value")
        .build();

    String actual = GraphQlValueHelper.getStringValue(value);

    assertEquals("string value", actual);
  }

  @Test
  void getStringValue_returnsString_forIntValue() {
    IntValue value = IntValue.newIntValue()
        .value(BigInteger.TEN)
        .build();

    String actual = GraphQlValueHelper.getStringValue(value);

    assertEquals("10", actual);
  }

  @Test
  void getStringValue_returnsString_forFloatValue() {
    FloatValue value = FloatValue.newFloatValue()
        .value(BigDecimal.valueOf(10.12))
        .build();

    String actual = GraphQlValueHelper.getStringValue(value);

    assertEquals("10.12", actual);
  }

  @Test
  void getStringValue_returnsString_forBooleanValue() {
    BooleanValue value = BooleanValue.newBooleanValue(false)
        .build();

    String actual = GraphQlValueHelper.getStringValue(value);

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

    String actual = GraphQlValueHelper.getStringValue(value);

    assertEquals("ObjectValue{objectFields=[ObjectField{name='field', value=StringValue{value='test'}}]}", actual);
  }

  @Test
  void getValue_returnsString_forStringValue() {
    StringValue value = StringValue.newStringValue("string value")
        .build();

    String actual = (String) GraphQlValueHelper.getValue(TypeName.newTypeName("String")
        .build(), value);

    assertEquals("string value", actual);
  }

  @Test
  void getValue_returnsDate_forNowStringValue() {
    LocalDate localDateBeforeTest = LocalDate.now();
    StringValue value = StringValue.newStringValue("NOW")
        .build();

    Object actual = GraphQlValueHelper.getValue(TypeName.newTypeName("Date")
        .build(), value);

    assertEquals("LocalDate", actual.getClass()
        .getSimpleName());
    assertEquals(localDateBeforeTest, actual);
    assertEquals(LocalDate.now(), actual);
  }
}
