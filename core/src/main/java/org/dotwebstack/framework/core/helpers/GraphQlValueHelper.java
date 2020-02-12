package org.dotwebstack.framework.core.helpers;

import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.language.Value;
import java.time.LocalDate;
import java.util.Objects;
import lombok.NonNull;

public class GraphQlValueHelper {

  private GraphQlValueHelper() {}

  public static Object getValue(@NonNull Type<?> type, @NonNull Value<?> value) {
    String stringValue = getStringValue(value);

    if ((type instanceof TypeName) && Objects.equals("Date", ((TypeName) type).getName())
        && Objects.equals("NOW", stringValue)) {
      return LocalDate.now();
    }
    return stringValue;
  }

  public static String getStringValue(@NonNull Value<?> value) {
    if (value instanceof IntValue) {
      return ((IntValue) value).getValue()
          .toString();
    } else if (value instanceof StringValue) {
      return ((StringValue) value).getValue();
    } else if (value instanceof FloatValue) {
      return ((FloatValue) value).getValue()
          .toString();
    } else if (value instanceof BooleanValue) {
      return Boolean.toString(((BooleanValue) value).isValue());
    }
    return value.toString();
  }
}
