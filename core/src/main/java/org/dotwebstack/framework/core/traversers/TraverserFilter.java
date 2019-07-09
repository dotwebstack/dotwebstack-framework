package org.dotwebstack.framework.core.traversers;

import graphql.schema.GraphQLDirectiveContainer;
import java.util.Map;
import java.util.Objects;

public interface TraverserFilter {

  boolean apply(GraphQLDirectiveContainer container, Map<String, Object> arguments);

  static TraverserFilter directiveFilter(String directiveName) {
    return (container, arguments) -> Objects.nonNull(container.getDirective(directiveName));
  }

  static TraverserFilter directiveWithValueFilter(String directiveName) {
    return (container, arguments) -> Objects.nonNull(container.getDirective(directiveName))
        && arguments.containsKey(container.getName());
  }

  static TraverserFilter noFilter() {
    return (container, arguments) -> Boolean.TRUE;
  }
}
