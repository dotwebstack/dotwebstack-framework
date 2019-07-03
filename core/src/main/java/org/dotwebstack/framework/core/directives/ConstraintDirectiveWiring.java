package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.validators.ConstraintValidator;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConstraintDirectiveWiring implements SchemaDirectiveWiring {

  private ConstraintValidator constraintValidator;

  @Override
  public GraphQLArgument onArgument(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    try {
      GraphQLArgument argument = environment.getElement();
      constraintValidator.validateArgument(argument);
    } catch (DirectiveValidationException exception) {
      throwConfigurationException(exception);
    }
    return environment.getElement();
  }

  @Override
  public GraphQLInputObjectField onInputObjectField(
      SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    try {
      constraintValidator.validateInputObjectField(environment.getElement());
    } catch (DirectiveValidationException exception) {
      throwConfigurationException(exception);
    }
    return environment.getElement();
  }

  private void throwConfigurationException(Exception cause) {
    throw new InvalidConfigurationException("Default value in constraint directive is violating constraint!", cause);
  }

}
