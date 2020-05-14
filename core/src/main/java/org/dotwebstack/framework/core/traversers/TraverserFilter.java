package org.dotwebstack.framework.core.traversers;

import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import java.util.Objects;

public interface TraverserFilter {

  boolean apply(DirectiveContainerObject tuple);

  static TraverserFilter directiveFilter(String directiveName) {
    return tuple -> Objects.nonNull(tuple.getContainer()
        .getDirective(directiveName));
  }

  static TraverserFilter directiveWithValueFilter(String directiveName) {
    return tuple -> Objects.nonNull(tuple.getContainer()
        .getDirective(directiveName)) && Objects.nonNull(tuple.getValue());
  }

  static TraverserFilter typeFilter(String name) {
    return tuple -> ((tuple.getContainer() instanceof GraphQLInputObjectField
        && Objects.equals(getTypeName(((GraphQLInputObjectField) tuple.getContainer()).getType()), name))
        || (tuple.getContainer() instanceof GraphQLArgument
            && Objects.equals(getTypeName(((GraphQLArgument) tuple.getContainer()).getType()), name)));
  }

  static TraverserFilter noFilter() {
    return tuple -> Boolean.TRUE;
  }
}
