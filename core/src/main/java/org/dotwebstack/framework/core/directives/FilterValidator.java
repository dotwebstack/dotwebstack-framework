package org.dotwebstack.framework.core.directives;

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
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.springframework.stereotype.Component;

@Component
public class FilterValidator {

  private final FilterDirectiveTraverser filterDirectiveTraverser;

  public FilterValidator(@NonNull FilterDirectiveTraverser filterDirectiveTraverser) {
    this.filterDirectiveTraverser = filterDirectiveTraverser;
  }

  void validateArgumentEnvironment(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    environment.getElementParentTree()
        .getParentInfo()
        .ifPresent(parentInfo -> {
          if (((GraphQLFieldDefinition) parentInfo.getElement()).getDirective(CoreDirectives.FILTER_NAME) == null) {
            throw ExceptionHelper.illegalArgumentException("'{}' can only be used as an argument for a Query!",
                environment.getElement());
          }

          GraphQLObjectType type = (GraphQLObjectType) GraphQLTypeUtil
              .unwrapAll(((GraphQLFieldDefinition) parentInfo.getElement()).getType());
          this.validate(environment.getElement(), environment.getRegistry(), type.getName());
        });
  }

  void validateInputObjectFieldEnvironment(SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    TypeDefinitionRegistry registry = environment.getRegistry();

    environment.getElementParentTree()
        .getParentInfo()
        .ifPresent(parentInfo -> {
          TypeDefinition<?> typeDefinition = registry.types()
              .get(parentInfo.getElement()
                  .getName());
          filterDirectiveTraverser.getReturnTypes(typeDefinition, registry)
              .forEach(typeName -> validate(environment.getElement(), registry, typeName));
        });
  }

  private void validate(GraphQLDirectiveContainer container, TypeDefinitionRegistry registry, String typeName) {
    GraphQLDirective directive = container.getDirective(CoreDirectives.FILTER_NAME);
    directive.getArguments()
        .forEach(
            directiveArgument -> this.validateArgument(directiveArgument, registry, directive.getName(), typeName));
  }

  private void validateArgument(GraphQLArgument argument, TypeDefinitionRegistry registry, String name,
      String typeName) {
    if (argument.getValue() != null) {
      switch (argument.getName()) {
        case CoreDirectives.FILTER_ARG_FIELD:
          checkField(argument, registry, name, typeName);
          break;
        case CoreDirectives.FILTER_ARG_OPERATOR:
          checkOperator(argument, name);
          break;
        default:
          throw new DirectiveValidationException("Unsupported filter argument with name '{}'", argument.getName());
      }
    }
  }

  public void checkField(GraphQLArgument argument, TypeDefinitionRegistry registry, String name, String typeName) {
    Optional<ObjectTypeDefinition> optional = registry.getType(typeName, ObjectTypeDefinition.class);
    if (!optional.isPresent() || optional.get()
        .getFieldDefinitions()
        .stream()
        .noneMatch(fieldDefinition -> fieldDefinition.getName()
            .equals(argument.getValue()))) {
      throw new DirectiveValidationException(
          "SparqlFilter 'field' [{}] on field '{}' is invalid. It does not exist on type '{}'", argument.getValue(),
          name, typeName);
    }
  }

  public void checkOperator(GraphQLArgument argument, String name) {
    if (argument.getValue() != null && !FilterOperator.getByValue(argument.getValue()
        .toString())
        .isPresent()) {
      throw new DirectiveValidationException(
          "SparqlFilter 'operator' [{}] on field '{}' is invalid. It should be one of: '=', '!=', '<', '<=', '>',"
              + " '>='",
          argument.getValue(), name);
    }
  }
}
