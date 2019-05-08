package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLInputObjectField;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConstraintDirectiveValidator implements DirectiveValidator {

  @Override
  public boolean supports(String directiveName) {
    return CoreDirectives.CONSTRAINT_NAME.equals(directiveName);
  }

  @Override
  public void onArgument(GraphQLDirective directive, GraphQLArgument argument,
                         Object value) {
    directive.getArguments().stream()
            .filter(directiveArgument -> directiveArgument.getValue() != null)
            .forEach(directiveArgument -> validate(directiveArgument,argument.getName(),value));
  }

  @Override
  public void onInputObjectField(GraphQLDirective directive,
                                 GraphQLInputObjectField inputObjectField, Object value) {
    directive.getArguments().stream()
            .filter(directiveArgument -> directiveArgument.getValue() != null)
            .forEach(directiveArgument -> validate(directiveArgument,
                    inputObjectField.getName(),value));
  }

  @SuppressWarnings("unchecked")
  private void validate(GraphQLArgument argument, String name, Object value) {
    switch (argument.getName()) {
      case "min":
        checkMin(name,(Integer)argument.getValue(),(Integer)value);
        break;
      case "max":
        checkMax(name,(Integer)argument.getValue(), (Integer) value);
        break;
      case "oneOf":
        checkOneOf(name,(List)argument.getValue(),value);
        break;
      default:
        throw new IllegalArgumentException("Unimplemented directive argument!");
    }
  }

  private void checkMin(String name, Integer constraint, Integer value)  {
    if (value < constraint) {
      throw new RuntimeException(
              String.format("Constraint 'min' on '%s' with value %s not satisfied!",name,value));
    }
  }

  private void checkMax(String name, Integer constraint, Integer value) {
    if (value > constraint) {
      throw new RuntimeException(
              String.format("Constraint 'max' on '%s' with value %s not satisfied!",name,value));
    }
  }

  private void checkOneOf(String name, List<Object> constraint, Object value) {
    if (!constraint.contains(value)) {
      throw new RuntimeException(
              String.format("Constraint 'oneOf' on '%s' with value %s not satisfied!",name,value));
    }
  }


}
