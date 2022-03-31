package org.dotwebstack.framework.core.jexl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;


class JexlHelperTest {

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  private final JexlHelper jexlHelper = new JexlHelper(this.jexlEngine);

  @Test
  void evaluateExpression_returns_value() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    final Optional<String> evaluated = this.jexlHelper.evaluateExpression("directiveValue1", context, String.class);

    assertThat("expected non-empty optional", evaluated.isPresent());
    assertThat(expectedValue, is(equalTo(evaluated.get())));
  }

  @Test
  void evaluateScript_returns_value() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    final Optional<String> evaluated =
        this.jexlHelper.evaluateScript("var result = `${key1}`; return result;", context, String.class);

    assertThat("expected non-empty optional", evaluated.isPresent());
    assertThat(expectedValue, is(equalTo(evaluated.get())));
  }

  @Test
  void evaluateScript_returns_null() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    final Optional<String> evaluated =
        this.jexlHelper.evaluateScript("var result = `${key1}`; return null;", context, String.class);

    assertThat("expected empty optional", evaluated.isEmpty());
  }

  @Test
  void evaluateExpression_throwsException_forTypeMismatch() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    assertThrows(IllegalArgumentException.class,
        () -> this.jexlHelper.evaluateExpression("directiveValue1", context, Integer.class));
  }

  @Test
  void evaluateScript_throwsException_forMissingContextProperties() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    InvalidConfigurationException invalidConfigurationException = assertThrows(InvalidConfigurationException.class,
        () -> this.jexlHelper.evaluateScript("`foo${bar}`", context, Object.class));

    assertThat(invalidConfigurationException.getMessage(), is("Error evaluating expression `foo${bar}`"));
  }

  @Test
  void evaluateScriptWithFallback_fallsBack_forExceptionInScript() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    Optional<String> optional =
        this.jexlHelper.evaluateScriptWithFallback("return 12;", "`${key1}`", context, String.class);
    assertEquals(expectedValue, optional.get());
  }

  @Test
  void evaluateScriptWithoutFallback_returnsNull_forExceptionInScript() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    Optional<String> optional = this.jexlHelper.evaluateScriptWithFallback("return 12;", null, context, String.class);
    assertEquals(Optional.empty(), optional);
  }

  @Test
  void evaluateScriptWithFallback_returnsNull_forExceptionInScriptAndFallback() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    Optional<String> optional =
        this.jexlHelper.evaluateScriptWithFallback("return 12;", "return 12;", context, String.class);
    assertEquals(Optional.empty(), optional);
  }

  @Test
  void createJexlContext_createsContext_forEnvAndArgParams() {
    Map<String, String> envParams = Map.of("key", "value");
    Map<String, Object> argParams = Map.of("name", "test");

    JexlContext context = JexlHelper.getJexlContext(envParams, null, argParams);

    assertEquals("value", evaluateExpressionToString("env.key", context));
    assertEquals("test", evaluateExpressionToString("args.name", context));
  }



  private String evaluateExpressionToString(String expression, JexlContext jexlContext) {
    return this.jexlHelper.evaluateExpression(expression, jexlContext, String.class)
        .orElse(null);
  }
}
