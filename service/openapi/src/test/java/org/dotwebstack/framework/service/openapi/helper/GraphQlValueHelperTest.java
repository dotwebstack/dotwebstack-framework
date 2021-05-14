package org.dotwebstack.framework.service.openapi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.dotwebstack.framework.core.helpers.GraphQlValueHelper;
import org.junit.jupiter.api.Test;

class GraphQlValueHelperTest {

  @Test
  void getStringValue_returnsValue_forSupportedTypes() {
    assertEquals("1", GraphQlValueHelper.getStringValue(new IntValue(BigInteger.ONE)));
    assertEquals("1", GraphQlValueHelper.getStringValue(new StringValue("1")));
    assertEquals("1", GraphQlValueHelper.getStringValue(new FloatValue(BigDecimal.ONE)));
    assertEquals("false", GraphQlValueHelper.getStringValue(new BooleanValue(false)));
  }
}
