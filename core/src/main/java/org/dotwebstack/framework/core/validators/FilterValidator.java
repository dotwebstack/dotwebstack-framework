package org.dotwebstack.framework.core.validators;

import static org.dotwebstack.framework.core.helpers.TypeHelper.getTypeName;

import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLNamedSchemaElement;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Objects;
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

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  public FilterValidator(@NonNull CoreTraverser coreTraverser, @NonNull TypeDefinitionRegistry typeDefinitionRegistry) {
    this.coreTraverser = coreTraverser;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  public void validateArgumentEnvironment(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    environment.getElementParentTree()
        .getParentInfo()
        .ifPresent(parentInfo -> {
          GraphQLType type = GraphQLTypeUtil.unwrapType(((GraphQLFieldDefinition) parentInfo.getElement()).getType())
              .lastElement();
          var typeName = TypeName.newTypeName(getTypeName(type))
              .build();

          if (!(type instanceof GraphQLTypeReference) && !GraphQLTypeUtil.isLeaf(type)
              || GraphQLTypeUtil.isList(type)) {
            this.validateDirectiveContainer(environment.getElement(), typeName);
          }
        });
  }

  public void validateInputObjectFieldEnvironment(
      SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {

    environment.getElementParentTree()
        .getParentInfo()
        .ifPresent(parentInfo -> {
          TypeDefinition<?> typeDefinition = typeDefinitionRegistry.types()
              .get(((GraphQLNamedSchemaElement) parentInfo.getElement()).getName());
          coreTraverser.getRootResultTypeNames(typeDefinition)
              .forEach(typeName -> validateDirectiveContainer(environment.getElement(), typeName));
        });
  }

  private void validateDirectiveContainer(GraphQLDirectiveContainer container, TypeName typeName) {
    GraphQLDirective directive = container.getDirective(CoreDirectives.FILTER_NAME);
    directive.getArguments()
        .forEach(directiveArgument -> this.validateArgument(directiveArgument, container.getName(), typeName));
  }

  private void validateArgument(GraphQLArgument argument, String name, TypeName typeName) {
    switch (argument.getName()) {
      case CoreDirectives.FILTER_ARG_FIELD:
        String fieldPath = (argument.getValue() != null) ? argument.getValue()
            .toString() : name;
        checkField(fieldPath, typeName);
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

  void checkField(String fieldPath, TypeName typeName) {
    ObjectTypeDefinition type = typeDefinitionRegistry.getType(typeName, ObjectTypeDefinition.class)
        .orElse(null);

    String[] fields = fieldPath.split("\\.");
    String fieldName = fields[0];

    if (Objects.nonNull(type)) {
      FieldDefinition definition = type.getFieldDefinitions()
          .stream()
          .filter(fieldDefinition -> fieldDefinition.getName()
              .equals(fieldName))
          .findFirst()
          .orElse(null);

      if (definition != null) {
        if (fields.length > 1) {
          TypeName fieldType = (TypeName) TypeHelper.getBaseType(definition.getType());
          checkField(fieldPath.substring(fieldPath.indexOf('.') + 1), fieldType);
        }
        return;
      }
    }
    throw new DirectiveValidationException("Filter field '{}' is invalid. It does not exist on type '{}'", fieldName,
        typeName);
  }

  void checkOperator(GraphQLArgument argument, String name) {
    if (argument.getValue() != null && !FilterOperator.getByValue(argument.getValue()
        .toString())
        .isPresent()) {
      throw new DirectiveValidationException(
          "Filter 'operator' [{}] on field '{}' is invalid. It should be one of: '=', '!=', '<', '<=', '>',"
              + " '>=', contains, iContains",
          argument.getValue(), name);
    }
  }
}
