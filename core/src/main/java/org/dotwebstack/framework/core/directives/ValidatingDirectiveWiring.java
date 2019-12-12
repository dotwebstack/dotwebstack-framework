package org.dotwebstack.framework.core.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;

abstract public class ValidatingDirectiveWiring {

  public void validate(String directiveName, GraphQLFieldDefinition fieldDefinition,
      GraphQLFieldsContainer fieldsContainer, Runnable validationActions) {
    try {
      validationActions.run();
    } catch (AssertionError error) {
      String typeName = fieldsContainer.getName();
      String fieldName = fieldDefinition.getName();

      throw invalidConfigurationException("[GraphQL] Found an error on @{} directive defined on {}.{}: {} ",
          directiveName, typeName, fieldName, error);
    }
  }

}
