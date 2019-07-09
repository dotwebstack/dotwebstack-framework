package org.dotwebstack.framework.core.traversers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.framework.core.traversers.TraverserFilter.directiveFilter;
import static org.dotwebstack.framework.core.traversers.TraverserFilter.directiveWithValueFilter;
import static org.dotwebstack.framework.core.traversers.TraverserFilter.noFilter;

import com.google.common.collect.ImmutableMap;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import org.junit.jupiter.api.Test;
import java.util.HashMap;

class TraverserFilterTest {

  @Test
  void directiveFilter_returns_true() {
    assertThat(directiveFilter("directive").apply(testArgument("directive"),ImmutableMap.of())).isTrue();
  }

  @Test
  void directiveFilter_returns_false() {
    assertThat(directiveFilter("directive").apply(testArgument("directive2"),ImmutableMap.of())).isFalse();
  }

  @Test
  void directiveFilterWithValue_return_true() {
    assertThat(directiveWithValueFilter("directive").apply(testArgument("directive"),
        ImmutableMap.of("arg","value"))).isTrue();
  }

  @Test
  void directiveFilterWithValue_return_false() {
    assertThat(directiveWithValueFilter("directive").apply(testArgument("directive"), ImmutableMap.of())).isFalse();
  }

  @Test
  void noFilter_returns_true() {
    assertThat(noFilter().apply(testArgument("directive2"),new HashMap<>())).isTrue();
  }

  private GraphQLArgument testArgument(String directiveName) {
    return GraphQLArgument.newArgument()
        .name("arg")
        .withDirective(
            GraphQLDirective.newDirective()
                .name(directiveName))
        .type(Scalars.GraphQLString)
        .build();
  }
}