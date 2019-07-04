package org.dotwebstack.framework.core.validators;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_MAX;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_MIN;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_ONEOF;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_ONEOF_INT;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_PATTERN;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLInputObjectField;
import java.util.List;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.springframework.stereotype.Component;

@Component
public class ConstraintValidator {

  private final CoreTraverser coreTraverser;

  public ConstraintValidator(CoreTraverser coreTraverser) {
    this.coreTraverser = coreTraverser;
  }

  public void validateDataFetchingEnvironment(DataFetchingEnvironment dataFetchingEnvironment) {
    coreTraverser.getInputObjectDirectiveContainers(dataFetchingEnvironment, CoreDirectives.CONSTRAINT_NAME)
        .forEach(this::validate);

    if (dataFetchingEnvironment.getSelectionSet() != null) {
      coreTraverser.getObjectTypes(dataFetchingEnvironment)
          .entrySet()
          .stream()
          .filter(entry -> entry.getKey()
              .getDirective(CoreDirectives.FILTER_NAME) != null)
          .forEach(entry -> validate(entry.getKey(), entry.getValue()));
    }
  }

  public void validateInputObjectField(GraphQLInputObjectField inputObjectField) {
    validate(inputObjectField, inputObjectField.getDefaultValue());
  }

  private void validate(GraphQLArgument argument, Object value) {
    validateArgument(argument, argument.getName(), ofNullable(value).orElse(argument.getDefaultValue()));
  }

  private void validate(GraphQLDirectiveContainer directiveContainer, Object value) {
    ofNullable(directiveContainer.getDirective(CoreDirectives.CONSTRAINT_NAME)).map(GraphQLDirective::getArguments)
        .ifPresent(directiveArguments -> directiveArguments
            .forEach(directiveArgument -> validateArgument(directiveArgument, directiveContainer.getName(), value)));
  }

  public void validateArgument(GraphQLArgument argument) {
    validate(argument, argument.getDefaultValue());
  }

  void validateArgument(GraphQLArgument argument, String name, Object value) {
    if (argument.getValue() != null) {
      switch (argument.getName()) {
        case CONSTRAINT_ARG_MIN:
          checkMin(name, (Integer) argument.getValue(), (Integer) value);
          break;
        case CONSTRAINT_ARG_MAX:
          checkMax(name, (Integer) argument.getValue(), (Integer) value);
          break;
        case CONSTRAINT_ARG_ONEOF:
        case CONSTRAINT_ARG_ONEOF_INT:
          checkOneOf(name, castToList(argument.getValue()), value);
          break;
        case CONSTRAINT_ARG_PATTERN:
          if (value != null) {
            checkPattern(name, argument.getValue()
                .toString(), value.toString());
          }
          break;
        default:
          throw new DirectiveValidationException("Unsupported constraint argument with name '{}'", argument.getName());
      }
    }
  }

  private void checkPattern(String name, String constraint, String value) {
    if (!value.matches(constraint)) {
      throw new DirectiveValidationException("Constraint 'pattern' [{}] violated on '{}' with value '{}'", constraint,
          name, value);
    }
  }

  private void checkMin(String name, Integer constraint, Integer value) {
    if (value < constraint) {
      throw new DirectiveValidationException("Constraint 'min' [{}] violated on '{}' with value '{}'", constraint, name,
          value);
    }
  }

  private void checkMax(String name, Integer constraint, Integer value) {
    if (value > constraint) {
      throw new DirectiveValidationException("Constraint 'max' [{}] violated on '{}' with value '{}'", constraint, name,
          value);
    }
  }

  private void checkOneOf(String name, List<Object> constraint, Object value) {
    if (!constraint.contains(value)) {
      throw new DirectiveValidationException("Constraint 'oneOf' {} violated on '{}' with value '{}'", constraint, name,
          value);
    }
  }
}
