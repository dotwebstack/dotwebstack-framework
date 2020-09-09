package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.traversers.DirectiveContainerObject;
import org.dotwebstack.framework.core.validators.ConstraintValidator;
import org.springframework.stereotype.Component;

@Component
public class ConstraintDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  private final ConstraintValidator constraintValidator;

  public ConstraintDirectiveWiring(@NonNull ConstraintValidator constraintValidator) {
    this.constraintValidator = constraintValidator;
  }

  @Override
  public String getDirectiveName() {
    return CoreDirectives.CONSTRAINT_NAME;
  }

  @Override
  public GraphQLArgument onArgument(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    try {
      GraphQLArgument argument = environment.getElement();
      constraintValidator.validateSchema(DirectiveContainerObject.builder()
          .container(argument)
          .value(argument.getDefaultValue())
          .fieldDefinition(environment.getFieldDefinition())
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
      constraintValidator.validateSchema(DirectiveContainerObject.builder()
          .container(inputObjectField)
          .value(inputObjectField.getDefaultValue())
          .fieldDefinition(environment.getFieldDefinition())
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
