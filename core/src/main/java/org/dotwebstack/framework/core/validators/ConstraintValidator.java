package org.dotwebstack.framework.core.validators;

import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_MAX;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_MIN;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_ONEOF;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_ONEOF_INT;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_ARG_PATTERN;
import static org.dotwebstack.framework.core.directives.CoreDirectives.CONSTRAINT_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToList;
import static org.dotwebstack.framework.core.traversers.TraverserFilter.directiveFilter;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLTypeUtil;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.dotwebstack.framework.core.traversers.DirectiveContainerObject;
import org.springframework.stereotype.Component;

@Component
public class ConstraintValidator implements QueryValidator {

  private final CoreTraverser coreTraverser;

  public ConstraintValidator(CoreTraverser coreTraverser) {
    this.coreTraverser = coreTraverser;
  }

  public void validateSchema(@NonNull DirectiveContainerObject directiveContainerObject) {
    validate(directiveContainerObject, false);
  }

  private void validateRequest(@NonNull DirectiveContainerObject directiveContainerObject) {
    validate(directiveContainerObject, true);
  }

  public void validate(DataFetchingEnvironment dataFetchingEnvironment) {
    coreTraverser
        .getTuples(dataFetchingEnvironment.getFieldDefinition(), dataFetchingEnvironment.getArguments(),
            directiveFilter(CONSTRAINT_NAME))
        .forEach(this::validateRequest);
  }

  private void validate(DirectiveContainerObject directiveContainerObject, boolean isRequest) {
    if (Objects.isNull(directiveContainerObject.getValue())) {
      if (isRequest) {
        validateRequiredValue(directiveContainerObject.getContainer());
      }
      return;
    }

    Stream.of(directiveContainerObject)
        .map(container -> directiveContainerObject.getContainer()
            .getDirective(CONSTRAINT_NAME))
        .flatMap(directive -> directive.getArguments()
            .stream())
        .forEach(argument -> validate(argument, directiveContainerObject.getContainer()
            .getName(), directiveContainerObject.getValue()));
  }

  void validate(GraphQLArgument argument, String name, Object value) {
    if (Objects.nonNull(argument.getValue())) {
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
          if (Objects.nonNull(value)) {
            checkPattern(name, argument.getValue()
                .toString(), value.toString());
          }
          break;
        default:
          throw new DirectiveValidationException("Unsupported constraint container with name '{}'", argument.getName());
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

  private void validateRequiredValue(GraphQLDirectiveContainer container) {
    Object defaultValue = null;
    GraphQLInputType inputType = null;

    if (container instanceof GraphQLArgument) {
      GraphQLArgument argument = (GraphQLArgument) container;
      defaultValue = argument.getDefaultValue();
      inputType = argument.getType();
    }

    if (container instanceof GraphQLInputObjectField) {
      GraphQLInputObjectField inputObjectField = (GraphQLInputObjectField) container;
      defaultValue = inputObjectField.getDefaultValue();
      inputType = inputObjectField.getType();
    }

    if (Objects.isNull(defaultValue) && GraphQLTypeUtil.isNonNull(inputType)) {
      throw illegalArgumentException("Required value for '{}' was not found", container.getName());
    }
  }
}
