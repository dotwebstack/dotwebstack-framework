package org.dotwebstack.framework.core.jexl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.junit.jupiter.api.Test;


public class JexlHelperTest {

  private final JexlEngine jexlEngine = new JexlBuilder()
      .silent(false)
      .strict(true)
      .create();
  private final JexlHelper jexlHelper = new JexlHelper(this.jexlEngine);

  @Test
  public void evaluateDirective_returns_value() {
    // Arrange
    final GraphQLDirective directive = getGraphQlDirective();
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(Map.of("directiveValue1", expectedValue));

    // Act
    final Optional<String> evaluated = this.jexlHelper.evaluateDirectiveArgument("directiveArg1",
        directive, context, String.class);

    // Assert
    assertThat("expected non-empty optional", !evaluated.isEmpty());
    assertThat(expectedValue, is(equalTo(evaluated.get())));
  }


  @Test
  public void evaluateDirective_returns_empty() {
    // Arrange
    final GraphQLDirective directive = getGraphQlDirective();
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(Map.of("directiveValue1", expectedValue));

    // Act
    final Optional<String> evaluated = this.jexlHelper.evaluateDirectiveArgument("directiveArg2",
        directive, context, String.class);

    // Assert
    assertThat("expected empty optional", evaluated.isEmpty());
  }

  @Test
  public void evaluateDirective_throwsException_forTypeMismatch() {
    // Arrange
    final GraphQLDirective directive = getGraphQlDirective();
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(Map.of("directiveValue1", expectedValue));

    // Assert
    assertThrows(IllegalArgumentException.class, () ->
        this.jexlHelper.evaluateDirectiveArgument("directiveArg1",
            directive, context, Integer.class)
    );
  }

  @Test
  public void evaluateExpression_returns_value() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(Map.of("directiveValue1", expectedValue));

    final Optional<String> evaluated = this.jexlHelper.evaluateExpression(
        "directiveValue1", context, String.class);

    // Assert
    assertThat("expected non-empty optional", !evaluated.isEmpty());
    assertThat(expectedValue, is(equalTo(evaluated.get())));
  }

  @Test
  public void evaluateExpression_throwsException_forTypeMismatch() {
    final String expectedValue = "value1";
    final JexlContext context = new MapContext(Map.of("directiveValue1", expectedValue));

    // Assert
    assertThrows(IllegalArgumentException.class, () ->
        this.jexlHelper.evaluateExpression("directiveValue1", context, Integer.class)
    );
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
