package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.NonNull;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.validators.FilterValidator;
import org.springframework.stereotype.Component;

@Component
public class FilterDirectiveWiring implements SchemaAutoRegisteredDirectiveWiring {

  private final FilterValidator validator;

  public FilterDirectiveWiring(@NonNull FilterValidator validator) {
    this.validator = validator;
  }

  @Override
  public String getDirectiveName() {
    return CoreDirectives.FILTER_NAME;
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
    throw new InvalidConfigurationException("A filter directive has been configured incorrectly.", cause);
  }
}
