package org.dotwebstack.framework.core.config;

import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class ValueUtils {

  private ValueUtils() {}

  @SuppressWarnings("rawtypes")
  public static Value newValue(ContextArgumentType type, Object value) {
    switch (type) {
      case DATE:
      case DATETIME:
      case STRING:
        return newStringValue(value);
      case BOOLEAN:
        return newBooleanValue(value);
      case INT:
        return newIntValue(value);
      case FLOAT:
        return newFloatValue(value);
      default:
        throw new IllegalArgumentException(String.format("Type %s can't be converted to Value.", type));
    }
  }

  private static StringValue newStringValue(Object value) {
    return StringValue.newStringValue((String) value)
        .build();
  }

  private static BooleanValue newBooleanValue(Object value) {
    return BooleanValue.newBooleanValue((boolean) value)
        .build();
  }

  private static IntValue newIntValue(Object value) {
    return IntValue.newIntValue(BigInteger.valueOf((int) value))
        .build();
  }

  private static FloatValue newFloatValue(Object value) {
    return FloatValue.newFloatValue(BigDecimal.valueOf((double) value))
        .build();
  }
}
