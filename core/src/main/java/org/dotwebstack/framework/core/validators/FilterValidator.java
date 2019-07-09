package org.dotwebstack.framework.core.validators;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.directives.FilterOperator;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.springframework.stereotype.Component;

@Component
public class FilterValidator {

  private final CoreTraverser coreTraverser;

  public FilterValidator(@NonNull CoreTraverser coreTraverser) {
    this.coreTraverser = coreTraverser;
  }

  public void validateArgumentEnvironment(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    environment.getElementParentTree()
        .getParentInfo()
        .ifPresent(parentInfo -> {
          GraphQLUnmodifiedType type =
              GraphQLTypeUtil.unwrapAll(((GraphQLFieldDefinition) parentInfo.getElement()).getType());

          if (!GraphQLTypeUtil.isLeaf(type) || GraphQLTypeUtil.isList(type)) {
            this.validateDirectiveContainer(environment.getElement(), environment.getRegistry(), type.getName());
          }
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
          coreTraverser.getPathToQuery(typeDefinition, registry)
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
        String fieldPath = (argument.getValue() != null) ? argument.getValue()
            .toString() : name;
        checkField(registry, fieldPath, typeName);
        break;
      case CoreDirectives.FILTER_ARG_OPERATOR:
        if (argument.getValue() != null) {
          checkOperator(argument, name);
        }
        break;
      default:
        throw new DirectiveValidationException("Unsupported filter container with name '{}'", argument.getName());
    }
  }

  void checkField(TypeDefinitionRegistry registry, String fieldPath, String typeName) {
    ObjectTypeDefinition type = registry.getType(typeName, ObjectTypeDefinition.class)
        .orElse(null);

    String[] fields = fieldPath.split("\\.");
    String fieldName = fields[0];

    if (type != null) {
      FieldDefinition definition = type.getFieldDefinitions()
          .stream()
          .filter(fieldDefinition -> fieldDefinition.getName()
              .equals(fieldName))
          .findFirst()
          .orElse(null);

      if (definition != null) {
        if (fields.length > 1) {
          TypeName fieldType = (TypeName) TypeHelper.getBaseType(definition.getType());
          checkField(registry, fieldPath.substring(fieldPath.indexOf(".") + 1), fieldType.getName());
        }
        return;
      }
    }
    throw new DirectiveValidationException("Filter 'field' [{}] is invalid. It does not exist on type '{}'", fieldName,
        typeName);
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
