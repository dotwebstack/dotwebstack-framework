package org.dotwebstack.framework.core.jexl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.junit.jupiter.api.Test;


class JexlHelperTest {

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  private final JexlHelper jexlHelper = new JexlHelper(this.jexlEngine);

  @Test
  void evaluateDirective_returns_value() {
    final GraphQLDirective directive = getGraphQlDirective();
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    final Optional<String> evaluated =
        this.jexlHelper.evaluateDirectiveArgument(directive, "directiveArg1", context, String.class);

    assertThat("expected non-empty optional", evaluated.isPresent());
    assertThat(expectedValue, is(equalTo(evaluated.get())));
  }


  @Test
  void evaluateDirective_returns_empty() {
    final GraphQLDirective directive = getGraphQlDirective();
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    final Optional<String> evaluated =
        this.jexlHelper.evaluateDirectiveArgument(directive, "directiveArg2", context, String.class);

    assertThat("expected empty optional", evaluated.isEmpty());
  }

  @Test
  void evaluateDirective_throwsException_forTypeMismatch() {
    final GraphQLDirective directive = getGraphQlDirective();
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    assertThrows(IllegalArgumentException.class,
        () -> this.jexlHelper.evaluateDirectiveArgument(directive, "directiveArg1", context, Integer.class));
  }

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
  void evaluateScript_throwsException_forTypeMismatch() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    assertThrows(IllegalArgumentException.class,
        () -> this.jexlHelper.evaluateScript("return 12;", context, String.class));
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

    JexlContext context = JexlHelper.getJexlContext(envParams, argParams);

    assertEquals("value", evaluateExpressionToString("env.key", context));
    assertEquals("test", evaluateExpressionToString("args.name", context));
  }

  @Test
  void createJexlContent_createsContext_forFieldDefinition() {
    GraphQLFieldDefinition fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name("beers")
        .type(Scalars.GraphQLString)
        .argument(GraphQLArgument.newArgument()
            .name("defaultValue")
            .type(Scalars.GraphQLString)
            .defaultValueProgrammatic("1")
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("value")
            .type(Scalars.GraphQLString)
            .valueProgrammatic("12")
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("both")
            .type(Scalars.GraphQLString)
            .defaultValueProgrammatic("1")
            .valueProgrammatic("12")
            .build())
        .build();

    JexlContext context = JexlHelper.getJexlContext(fieldDefinition);

    assertEquals("1", evaluateExpressionToString("args.defaultValue", context));
    assertEquals("12", evaluateExpressionToString("args.value", context));
    assertEquals("12", evaluateExpressionToString("args.both", context));
  }

  private GraphQLDirective getGraphQlDirective() {
    return GraphQLDirective.newDirective()
        .name("directive")
        .argument(GraphQLArgument.newArgument()
            .name("directiveArg1")
            .type(Scalars.GraphQLString)
            .valueProgrammatic("directiveValue1"))
        .build();
  }

  private String evaluateExpressionToString(String expression, JexlContext jexlContext) {
    return this.jexlHelper.evaluateExpression(expression, jexlContext, String.class)
        .orElse(null);
  }
}
