package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConstraintDirectiveWiring implements SchemaDirectiveWiring {

  private ConstraintDirectiveValidator constraintDirectiveValidator;

  @Override
  public GraphQLArgument onArgument(
          SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    try {
      constraintDirectiveValidator.onArgument(environment.getDirective(), environment.getElement(),
              environment.getElement().getDefaultValue());
    } catch (DirectiveValidationException e) {
      throw new InvalidConfigurationException(
              "Default value in constraint directive is violating constraint!",e);
    }
    return environment.getElement();
  }

  @Override
  public GraphQLInputObjectField onInputObjectField(
          SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    try {
      constraintDirectiveValidator.onInputObjectField(environment.getDirective(),
              environment.getElement(), environment.getElement().getDefaultValue());
    } catch (DirectiveValidationException e) {
      throw new InvalidConfigurationException(
              "Default value in constraint directive is violating constraint!",e);
    }
    return environment.getElement();
  }

}
