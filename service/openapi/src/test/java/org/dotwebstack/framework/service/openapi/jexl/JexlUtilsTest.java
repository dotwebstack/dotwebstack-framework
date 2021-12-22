package org.dotwebstack.framework.service.openapi.jexl;

import static org.dotwebstack.framework.core.jexl.JexlHelper.getJexlContext;
import static org.dotwebstack.framework.service.openapi.jexl.JexlUtils.evaluateJexlExpression;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JexlUtilsTest {

  private JexlHelper jexlHelper;

  private JexlContext jexlContext;

  @BeforeEach
  void beforeEach() {
    var envProperties = Map.<String, String>of("foo", "bar");
    var argProperties = Map.<String, Object>of("baz", "qux");
    jexlContext = getJexlContext(envProperties, null, argProperties);

    jexlHelper = new JexlHelper(new JexlBuilder().silent(false)
        .strict(true)
        .create());
  }

  @Test
  void evaluateJexlExpression_givenArgs_returnsCorrectly() {
    var jexlExpression = JexlExpression.builder()
        .value("env.foo + args.baz")
        .build();

    var optionalResult = evaluateJexlExpression(jexlExpression, jexlHelper, jexlContext, Object.class);

    assertThat(optionalResult.isPresent(), is(true));

    var result = optionalResult.get();
    assertThat(result, is("barqux"));
  }

  @Test
  void evaluateJexlExpression_givenFallback_returnsCorrectly() {
    var jexlExpression = JexlExpression.builder()
        .value("missing")
        .fallback("args.baz")
        .build();

    var optionalResult = evaluateJexlExpression(jexlExpression, jexlHelper, jexlContext, Object.class);

    assertThat(optionalResult.isPresent(), is(true));

    var result = optionalResult.get();
    assertThat(result, is("qux"));
  }
}
