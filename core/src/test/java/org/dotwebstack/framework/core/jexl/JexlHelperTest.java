package org.dotwebstack.framework.core.jexl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import graphql.Scalars;
import graphql.language.StringValue;
import graphql.language.TypeName;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.query.GraphQlArgument;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.junit.jupiter.api.Test;


public class JexlHelperTest {

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  private final JexlHelper jexlHelper = new JexlHelper(this.jexlEngine);

  @Test
  public void evaluateDirective_returns_value() {
    // Arrange
    final GraphQLDirective directive = getGraphQlDirective();
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    // Act
    final Optional<String> evaluated =
        this.jexlHelper.evaluateDirectiveArgument(directive, "directiveArg1", context, String.class);

    // Assert
    assertThat("expected non-empty optional", evaluated.isPresent());
    assertThat(expectedValue, is(equalTo(evaluated.get())));
  }


  @Test
  public void evaluateDirective_returns_empty() {
    // Arrange
    final GraphQLDirective directive = getGraphQlDirective();
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    // Act
    final Optional<String> evaluated =
        this.jexlHelper.evaluateDirectiveArgument(directive, "directiveArg2", context, String.class);

    // Assert
    assertThat("expected empty optional", !evaluated.isPresent());
  }

  @Test
  public void evaluateDirective_throwsException_forTypeMismatch() {
    // Arrange
    final GraphQLDirective directive = getGraphQlDirective();
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    // Act / Assert
    assertThrows(IllegalArgumentException.class,
        () -> this.jexlHelper.evaluateDirectiveArgument(directive, "directiveArg1", context, Integer.class));
  }

  @Test
  public void evaluateExpression_returns_value() {
    // Arrange
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    // Act
    final Optional<String> evaluated = this.jexlHelper.evaluateExpression("directiveValue1", context, String.class);

    // Assert
    assertThat("expected non-empty optional", evaluated.isPresent());
    assertThat(expectedValue, is(equalTo(evaluated.get())));
  }

  @Test
  public void evaluateScript_returns_value() {
    // Arrange
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    // Act
    final Optional<String> evaluated =
        this.jexlHelper.evaluateScriptWithFallback("var result = `${key1}`; return result;", context, String.class);

    // Assert
    assertThat("expected non-empty optional", evaluated.isPresent());
    assertThat(expectedValue, is(equalTo(evaluated.get())));
  }

  @Test
  public void evaluateScript_returns_null() {
    // Arrange
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    // Act
    final Optional<String> evaluated =
        this.jexlHelper.evaluateScriptWithFallback("var result = `${key1}`; return null;", context, String.class);

    // Assert
    assertThat("expected empty optional", !evaluated.isPresent());
  }

  @Test
  public void evaluateExpression_throwsException_forTypeMismatch() {
    // Arrange
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("directiveValue1", expectedValue));

    // Act / Assert
    assertThrows(IllegalArgumentException.class,
        () -> this.jexlHelper.evaluateExpression("directiveValue1", context, Integer.class));
  }

  @Test
  public void evaluateScript_throwsException_forTypeMismatch() {
    // Arrange
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    // Act/ Assert
    assertThrows(IllegalArgumentException.class,
        () -> this.jexlHelper.evaluateScriptWithFallback("return 12;", context, String.class));
  }

  @Test
  public void evalueScriptWithFallback_fallsBack_forExceptionInScript() {
    // Arrange
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    // Act/ Assert
    Optional<String> optional =
        this.jexlHelper.evaluateScriptWithFallback("return 12;", "`${key1}`", context, String.class);
    assertEquals(expectedValue, optional.get());
  }

  @Test
  public void evalueScriptWithoutFallback_returnsNull_forExceptionInScript() {
    // Arrange
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    // Act/ Assert
    Optional<String> optional = this.jexlHelper.evaluateScriptWithFallback("return 12;", null, context, String.class);
    assertEquals(Optional.empty(), optional);
  }

  @Test
  public void evalueScriptWithFallback_returnsNull_forExceptionInScriptAndFallback() {
    // Arrange
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(ImmutableMap.of("key1", expectedValue));

    // Act/ Assert
    Optional<String> optional =
        this.jexlHelper.evaluateScriptWithFallback("return 12;", "return 12;", context, String.class);
    assertEquals(Optional.empty(), optional);
  }

  @Test
  public void createJexlContext_createsContext_forEnvAndArgParams() {
    // Arrange
    Map<String, String> envParams = Map.of("key", "value");
    Map<String, Object> argParams = Map.of("name", "test");

    JexlContext context = JexlHelper.getJexlContext(envParams, argParams);

    assertEquals("value", context.get("env.key"));
    assertEquals("test", context.get("args.name"));
  }

  @Test
  public void createJexlContext_createsContext_forGraphQlArgument() {
    // Arrange
    GraphQlField graphQlField = GraphQlField.builder()
        .arguments(List.of(GraphQlArgument.builder()
            .name("value")
            .type(TypeName.newTypeName("String")
                .build())
            .defaultValue(StringValue.newStringValue("1")
                .build())
            .build()))
        .build();

    JexlContext context = JexlHelper.getJexlContext(null, null, graphQlField);

    assertEquals("1", context.get("args.value"));
  }

  private GraphQLDirective getGraphQlDirective() {
    return GraphQLDirective.newDirective()
        .name("directive")
        .argument(GraphQLArgument.newArgument()
            .name("directiveArg1")
            .type(Scalars.GraphQLString)
            .value("directiveValue1"))
        .build();
  }
}
