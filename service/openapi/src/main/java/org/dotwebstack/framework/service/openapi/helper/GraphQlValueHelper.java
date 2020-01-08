package org.dotwebstack.framework.service.openapi.helper;

import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import lombok.NonNull;

public class GraphQlValueHelper {

  public GraphQlValueHelper() {}

  public String getStringValue(@NonNull Value<?> value) {
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
