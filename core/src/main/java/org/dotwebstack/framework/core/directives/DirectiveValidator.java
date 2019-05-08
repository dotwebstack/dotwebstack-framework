package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputObjectField;

public interface DirectiveValidator {
  boolean supports(String directiveName);

  void onArgument(GraphQLDirective directive, GraphQLArgument argument, Object value);

  void onInputObjectField(GraphQLDirective directive,
                            GraphQLInputObjectField inputObjectField, Object value);
}
