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

public class GraphQlValueHelperTest {

  @Test
  public void getStringValue_returnsString_forStringValue() {
    // Arrange
    StringValue value = StringValue.newStringValue("string value")
        .build();

    // Act
    String actual = GraphQlValueHelper.getStringValue(value);

    // Assert
    assertEquals("string value", actual);
  }

  @Test
  public void getStringValue_returnsString_forIntValue() {
    // Arrange
    IntValue value = IntValue.newIntValue()
        .value(BigInteger.TEN)
        .build();

    // Act
    String actual = GraphQlValueHelper.getStringValue(value);

    // Assert
    assertEquals("10", actual);
  }

  @Test
  public void getStringValue_returnsString_forFloatValue() {
    // Arrange
    FloatValue value = FloatValue.newFloatValue()
        .value(BigDecimal.valueOf(10.12))
        .build();

    // Act
    String actual = GraphQlValueHelper.getStringValue(value);

    // Assert
    assertEquals("10.12", actual);
  }

  @Test
  public void getStringValue_returnsString_forBooleanValue() {
    // Arrange
    BooleanValue value = BooleanValue.newBooleanValue(false)
        .build();

    // Act
    String actual = GraphQlValueHelper.getStringValue(value);

    // Assert
    assertEquals("false", actual);
  }

  @Test
  public void getStringValue_returnsString_forOtherValue() {
    // Arrange
    ObjectValue value = ObjectValue.newObjectValue()
        .objectField(ObjectField.newObjectField()
            .name("field")
            .value(StringValue.newStringValue("test")
                .build())
            .build())
        .build();

    // Act
    String actual = GraphQlValueHelper.getStringValue(value);

    // Assert
    assertEquals("ObjectValue{objectFields=[ObjectField{name='field', value=StringValue{value='test'}}]}", actual);
  }

  @Test
  public void getValue_returnsString_forStringValue() {
    // Arrange
    StringValue value = StringValue.newStringValue("string value")
        .build();

    // Act
    String actual = (String) GraphQlValueHelper.getValue(TypeName.newTypeName("String")
        .build(), value);

    // Assert
    assertEquals("string value", actual);
  }

  @Test
  public void getValue_returnsDate_forNowStringValue() {
    // Arrange
    LocalDate localDateBeforeTest = LocalDate.now();
    StringValue value = StringValue.newStringValue("NOW")
        .build();

    // Act
    Object actual = GraphQlValueHelper.getValue(TypeName.newTypeName("Date")
        .build(), value);

    // Assert
    assertEquals("LocalDate", actual.getClass()
        .getSimpleName());
    assertEquals(localDateBeforeTest, actual);
    assertEquals(LocalDate.now(), actual);
  }
}
