package org.dotwebstack.framework.core.validators;

import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Optional;
import lombok.NonNull;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.dotwebstack.framework.core.traversers.FilterDirectiveTraverser;
import org.springframework.stereotype.Component;

@Component
public class FilterValidator {

  private final FilterDirectiveTraverser filterDirectiveTraverser;

  public FilterValidator(@NonNull FilterDirectiveTraverser filterDirectiveTraverser) {
    this.filterDirectiveTraverser = filterDirectiveTraverser;
  }

  public void validateArgumentEnvironment(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    environment.getElementParentTree()
        .getParentInfo()
        .ifPresent(parentInfo -> {
          GraphQLObjectType type = (GraphQLObjectType) GraphQLTypeUtil
              .unwrapAll(((GraphQLFieldDefinition) parentInfo.getElement()).getType());
          this.validateDirectiveContainer(environment.getElement(), environment.getRegistry(), type.getName());
        });
  }

  public void validateInputObjectFieldEnvironment(
      SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    TypeDefinitionRegistry registry = environment.getRegistry();

    environment.getElementParentTree()
        .getParentInfo()
        .ifPresent(parentInfo -> {
          TypeDefinition<?> typeDefinition = registry.types()
              .get(parentInfo.getElement()
                  .getName());
          filterDirectiveTraverser.getPathToQuery(typeDefinition, registry)
              .forEach(typeName -> validateDirectiveContainer(environment.getElement(), registry, typeName));
        });
  }

  private void validateDirectiveContainer(GraphQLDirectiveContainer container, TypeDefinitionRegistry registry,
      String typeName) {
    GraphQLDirective directive = container.getDirective(CoreDirectives.FILTER_NAME);
    directive.getArguments()
        .forEach(
            directiveArgument -> this.validateArgument(directiveArgument, registry, container.getName(), typeName));
  }

  private void validateArgument(GraphQLArgument argument, TypeDefinitionRegistry registry, String name,
      String typeName) {
    switch (argument.getName()) {
      case CoreDirectives.FILTER_ARG_FIELD:
        checkField(argument, registry, name, typeName);
        break;
      case CoreDirectives.FILTER_ARG_OPERATOR:
        if (argument.getValue() != null) {
          checkOperator(argument, name);
        }
        break;
      default:
        throw new DirectiveValidationException("Unsupported filter argument with name '{}'", argument.getName());
    }
  }

  void checkField(GraphQLArgument argument, TypeDefinitionRegistry registry, String queryArgumentName,
      String typeName) {
    Optional<ObjectTypeDefinition> optional = registry.getType(typeName, ObjectTypeDefinition.class);
    String fieldName = (argument.getValue() != null) ? argument.getValue()
        .toString() : queryArgumentName;
    if (!optional.isPresent() || optional.get()
        .getFieldDefinitions()
        .stream()
        .noneMatch(fieldDefinition -> fieldDefinition.getName()
            .equals(fieldName))) {

      throw new DirectiveValidationException(
          "Filter 'field' [{}] on field '{}' is invalid. It does not exist on type '{}'", argument.getValue(),
          queryArgumentName, typeName);
    }
  }

  void checkOperator(GraphQLArgument argument, String name) {
    if (argument.getValue() != null && !FilterOperator.getByValue(argument.getValue()
        .toString())
        .isPresent()) {
      throw new DirectiveValidationException(
          "Filter 'operator' [{}] on field '{}' is invalid. It should be one of: '=', '!=', '<', '<=', '>'," + " '>='",
          argument.getValue(), name);
    }
  }
}
