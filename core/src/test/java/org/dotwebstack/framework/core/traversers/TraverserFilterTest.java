package org.dotwebstack.framework.core.traversers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.framework.core.traversers.TraverserFilter.directiveFilter;
import static org.dotwebstack.framework.core.traversers.TraverserFilter.directiveWithValueFilter;
import static org.dotwebstack.framework.core.traversers.TraverserFilter.noFilter;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import org.junit.jupiter.api.Test;

class TraverserFilterTest {

  @Test
  void directiveFilter_returns_true() {
    assertThat(directiveFilter("directive").apply(testTuple("directive", null))).isTrue();
  }

  @Test
  void directiveFilter_returns_false() {
    assertThat(directiveFilter("directive").apply(testTuple("directive2", null))).isFalse();
  }

  @Test
  void directiveFilterWithValue_return_true() {
    assertThat(directiveWithValueFilter("directive").apply(testTuple("directive", "value"))).isTrue();
  }

  @Test
  void directiveFilterWithValue_return_false() {
    assertThat(directiveWithValueFilter("directive").apply(testTuple("directive", null))).isFalse();
  }

  @Test
  void noFilter_returns_true() {
    assertThat(noFilter().apply(null)).isTrue();
  }

  private DirectiveContainerTuple testTuple(String directiveName, Object value) {
    return new DirectiveContainerTuple(GraphQLArgument.newArgument()
        .name("arg")
        .withDirective(GraphQLDirective.newDirective()
            .name(directiveName))
        .type(Scalars.GraphQLString)
        .build(), value);
  }
}
