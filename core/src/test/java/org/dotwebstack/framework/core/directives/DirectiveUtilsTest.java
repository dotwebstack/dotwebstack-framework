package org.dotwebstack.framework.core.directives;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;

class DirectiveUtilsTest {

  @Test
  void getStringArgument_returnsString_forPresentArgument() {
    // Arrange
    GraphQLDirective directive = GraphQLDirective.newDirective()
        .name("foo")
        .argument(GraphQLArgument.newArgument()
            .name("foo")
            .type(Scalars.GraphQLString)
            .valueProgrammatic("bar"))
        .build();

    // Act
    String value = DirectiveUtils.getArgument(directive, "foo", String.class);

    // Assert
    assertThat(value, is(equalTo("bar")));
  }

  @Test
  void getStringArgument_returnsNull_forAbsentArgument() {
    // Arrange
    GraphQLDirective directive = GraphQLDirective.newDirective()
        .name("foo")
        .build();

    // Act
    String value = DirectiveUtils.getArgument(directive, "foo", String.class);

    // Assert
    assertThat(value, is(nullValue()));
  }

  @Test
  void getStringArgument_returnsNull_forArgumentWithNullValue() {
    // Arrange
    GraphQLDirective directive = GraphQLDirective.newDirective()
        .name("foo")
        .argument(GraphQLArgument.newArgument()
            .name("foo")
            .type(Scalars.GraphQLString))
        .build();

    // Act
    String value = DirectiveUtils.getArgument(directive, "foo", String.class);

    // Assert
    assertThat(value, is(nullValue()));
  }

  @Test
  void getStringArgument_throwsException_forMistypedArgument() {
    // Arrange
    GraphQLDirective directive = GraphQLDirective.newDirective()
        .name("foo")
        .argument(GraphQLArgument.newArgument()
            .name("foo")
            .type(Scalars.GraphQLBoolean)
            .valueProgrammatic(true))
        .build();

    // Act / Assert
    assertThrows(InvalidConfigurationException.class, () -> DirectiveUtils.getArgument(directive, "foo", String.class));
  }

}
