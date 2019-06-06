package org.dotwebstack.framework.backend.rdf4j.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.springframework.stereotype.Component;

@Component
public class SparqlFilterDirectiveWiring implements SchemaDirectiveWiring {

  private final SparqlFilterValidator validator;

  public SparqlFilterDirectiveWiring(@NonNull SparqlFilterValidator validator) {
    this.validator = validator;
  }

  @Override
  public GraphQLArgument onArgument(@NonNull SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    try {
      validator.validateArgumentEnvironment(environment);
    } catch (DirectiveValidationException exception) {
      throwConfigurationException(exception);
    }
    return environment.getElement();
  }

  @Override
  public GraphQLInputObjectField onInputObjectField(
      @NonNull SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    try {
      validator.validateInputObjectFieldEnvironment(environment);
    } catch (DirectiveValidationException exception) {
      throwConfigurationException(exception);
    }
    return environment.getElement();
  }

  private void throwConfigurationException(Exception cause) {
    throw new InvalidConfigurationException("Default value in constraint directive is violating constraint!", cause);
  }
}
