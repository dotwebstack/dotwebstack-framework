package org.dotwebstack.framework.core.traversers;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLNamedType;
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
    return tuple -> ((tuple.getContainer() instanceof GraphQLInputObjectField && Objects
        .equals(((GraphQLNamedType) ((GraphQLInputObjectField) tuple.getContainer()).getType()).getName(), name))
        || (tuple.getContainer() instanceof GraphQLArgument && Objects
            .equals(((GraphQLNamedType) ((GraphQLArgument) tuple.getContainer()).getType()).getName(), name)));
  }

  static TraverserFilter noFilter() {
    return tuple -> Boolean.TRUE;
  }
}
