package org.dotwebstack.framework.core.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

public abstract class PagingDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  @Override
  public GraphQLArgument onArgument(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    if (!GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(environment.getFieldDefinition()
        .getType()))) {
      throw invalidConfigurationException("{} directive can only be added to a list field", getDirectiveName());
    }

    if (environment.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> argument.getDirective(getDirectiveName()) != null)
        .count() > 1) {
      throw invalidConfigurationException("{} field on {} object contains more than one {} directive",
          environment.getFieldDefinition()
              .getName(),
          environment.getElementParentTree()
              .getParentInfo()
              .flatMap(parent -> parent.getParentInfo()
                  .map(grandparent -> grandparent.getElement()
                      .getName()))
              .orElse(null),
          getDirectiveName());
    }

    return environment.getElement();
  }

}
