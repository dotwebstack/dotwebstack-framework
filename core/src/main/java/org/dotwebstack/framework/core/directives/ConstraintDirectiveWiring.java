package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.traversers.DirectiveContainerTuple;
import org.dotwebstack.framework.core.validators.ConstraintValidator;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConstraintDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  private ConstraintValidator constraintValidator;

  @Override
  public String getDirectiveName() {
    return CoreDirectives.CONSTRAINT_NAME;
  }

  @Override
  public GraphQLArgument onArgument(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    try {
      GraphQLArgument argument = environment.getElement();
      constraintValidator.validateSchema(DirectiveContainerTuple.builder()
          .container(argument)
          .value(argument.getDefaultValue())
          .build());
    } catch (DirectiveValidationException exception) {
      throwConfigurationException(exception);
    }
    return environment.getElement();
  }

  @Override
  public GraphQLInputObjectField onInputObjectField(
      SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    try {
      GraphQLInputObjectField inputObjectField = environment.getElement();
      constraintValidator.validateSchema(DirectiveContainerTuple.builder()
          .container(inputObjectField)
          .value(inputObjectField.getDefaultValue())
          .build());
    } catch (DirectiveValidationException exception) {
      throwConfigurationException(exception);
    }
    return environment.getElement();
  }

  private void throwConfigurationException(Exception cause) {
    throw new InvalidConfigurationException("Default value in constraint directive is violating constraint!", cause);
  }

}
