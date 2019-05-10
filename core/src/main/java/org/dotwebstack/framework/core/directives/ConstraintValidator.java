package org.dotwebstack.framework.core.directives;

import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_MAX;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_MIN;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_ONEOF;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_ONEOF_INT;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;

import graphql.schema.GraphQLArgument;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ConstraintValidator {

  public void validate(GraphQLArgument argument, String name, Object value) {

    if (argument.getValue() == null) {
      return;
    }

    switch (argument.getName()) {
      case CONSTRAINT_ARG_MIN:
        checkMin(name,(Integer) argument.getValue(),(Integer) value);
        break;
      case CONSTRAINT_ARG_MAX:
        checkMax(name,(Integer) argument.getValue(),(Integer) value);
        break;
      case CONSTRAINT_ARG_ONEOF:
      case CONSTRAINT_ARG_ONEOF_INT:
        checkOneOf(name, castToList(argument.getValue()),value);
        break;
      default:
        throw new DirectiveValidationException("Unsupported constraint argument with name '{}'");
    }
  }

  private void checkMin(String name, Integer constraint, Integer value)  {
    if (value < constraint) {
      throw new DirectiveValidationException(
              "Constraint 'min' [{}] violated on '{}' with value '{}'",constraint,name,value);
    }
  }

  private void checkMax(String name, Integer constraint, Integer value) {
    if (value > constraint) {
      throw new DirectiveValidationException(
              "Constraint 'max' [{}] violated on '{}' with value '{}'",constraint,name,value);
    }
  }

  private void checkOneOf(String name, List<Object> constraint, Object value) {
    if (!constraint.contains(value)) {
      throw new DirectiveValidationException(
              "Constraint 'oneOf' {} violated on '{}' with value '{}'",constraint,name,value);
    }
  }
}
