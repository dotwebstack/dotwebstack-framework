package org.dotwebstack.framework.core.traversers;

import java.util.Objects;

public interface TraverserFilter {

  boolean apply(DirectiveContainerTuple tuple);

  static TraverserFilter directiveFilter(String directiveName) {
    return tuple -> Objects.nonNull(tuple.getContainer()
        .getDirective(directiveName));
  }

  static TraverserFilter directiveWithValueFilter(String directiveName) {
    return tuple -> Objects.nonNull(tuple.getContainer()
        .getDirective(directiveName)) && Objects.nonNull(tuple.getValue());
  }

  static TraverserFilter noFilter() {
    return tuple -> Boolean.TRUE;
  }
}
