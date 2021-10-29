package org.dotwebstack.framework.service.openapi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.dotwebstack.framework.core.helpers.GraphQlHelper;
import org.junit.jupiter.api.Test;

class GraphQlHelperTest {

  @Test
  void getStringValue_returnsValue_forSupportedTypes() {
    assertEquals("1", GraphQlHelper.getStringValue(new IntValue(BigInteger.ONE)));
    assertEquals("1", GraphQlHelper.getStringValue(new StringValue("1")));
    assertEquals("1", GraphQlHelper.getStringValue(new FloatValue(BigDecimal.ONE)));
    assertEquals("false", GraphQlHelper.getStringValue(new BooleanValue(false)));
  }
}
