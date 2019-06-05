package org.dotwebstack.framework.backend.rdf4j.directives;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphqlElementParentTree;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Optional;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.input.CoreTypes;
import org.springframework.stereotype.Component;

@Component
public class SparqlFilterValidator {

  void validateArgumentEnvironment(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    environment.getElementParentTree()
        .getParentInfo()
        .ifPresent(parentInfo -> {
          GraphqlElementParentTree grandParentInfo = parentInfo.getParentInfo()
              .orElseThrow(() -> ExceptionHelper.illegalArgumentException("Did not find a parent type for `{}`",
                  environment.getElement()));

          if (!grandParentInfo.getElement()
              .getName()
              .equals(CoreTypes.QUERY_KEYWORD)) {
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
          traverse(environment.getElement(), typeDefinition, registry);
        });

  }

  private void traverse(GraphQLDirectiveContainer container, TypeDefinition<?> baseType,
      TypeDefinitionRegistry registry) {
    registry.types()
        .keySet()
        .forEach(item -> registry.getType(item)
            .ifPresent(compareType -> {
              if (compareType instanceof ObjectTypeDefinition) {
                if (item.equals(CoreTypes.QUERY_KEYWORD)) {
                  processQuery(container, registry, baseType, (ObjectTypeDefinition) compareType);
                } else {
                  processObjectType(container, registry, baseType, compareType);
                }
              } else if (compareType instanceof InputObjectTypeDefinition) {
                processInputObjectType(container, registry, baseType, compareType);
              }
            }));
  }

  private void processInputObjectType(GraphQLDirectiveContainer container, TypeDefinitionRegistry registry,
      TypeDefinition<?> baseType, TypeDefinition<?> compareType) {
    ((InputObjectTypeDefinition) compareType).getInputValueDefinitions()
        .stream()
        .filter(inputValue -> registry.getType(getBaseType(inputValue.getType()))
            .map(definition -> definition.equals(baseType))
            .orElse(false))
        .findAny()
        .ifPresent(definition -> traverse(container, compareType, registry));
  }

  private void processObjectType(GraphQLDirectiveContainer container, TypeDefinitionRegistry registry,
      TypeDefinition<?> parentType, TypeDefinition<?> compareType) {
    ((ObjectTypeDefinition) compareType).getFieldDefinitions()
        .stream()
        .filter(inputField -> registry.getType(getBaseType(inputField.getType()))
            .map(definition -> definition.equals(parentType))
            .orElse(false))
        .findAny()
        .ifPresent(inputValueDefinition -> traverse(container, compareType, registry));
  }

  private void processQuery(GraphQLDirectiveContainer container, TypeDefinitionRegistry registry,
      TypeDefinition<?> parentType, ObjectTypeDefinition compareType) {
    compareType.getFieldDefinitions()
        .stream()
        .filter(inputField -> inputField.getInputValueDefinitions()
            .stream()
            .anyMatch(inputValueDefinition -> registry.getType(getBaseType(inputValueDefinition.getType()))
                .map(definition -> definition.equals(parentType))
                .orElse(false)))
        .forEach(inputField -> validate(container, registry, ((TypeName) getBaseType(inputField.getType())).getName()));
  }

  private void validate(GraphQLDirectiveContainer container, TypeDefinitionRegistry registry, String typeName) {
    GraphQLDirective directive = container.getDirective(Rdf4jDirectives.SPARQL_FILTER_NAME);
    this.validateDirective(directive, directive.getName());
    directive.getArguments()
        .forEach(
            directiveArgument -> this.validateArgument(directiveArgument, registry, directive.getName(), typeName));
  }

  void validateDirective(GraphQLDirective directive, String fieldName) {
    GraphQLArgument expr = directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR);
    GraphQLArgument operator = directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR);

    if (expr.getValue() != null && operator.getValue() != null) {
      throw new DirectiveValidationException(
          "Found both an expression and an operator argument on sparql filter directive on field '{}'", fieldName);
    }
  }

  private void validateArgument(GraphQLArgument argument, TypeDefinitionRegistry registry, String name,
      String typeName) {
    if (argument.getValue() != null) {
      switch (argument.getName()) {
        case Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR:
          break;
        case Rdf4jDirectives.SPARQL_FILTER_ARG_FIELD:
          checkField(argument, registry, name, typeName);
          break;
        case Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR:
          checkOperator(argument, name);
          break;
        default:
          throw new DirectiveValidationException("Unsupported filter argument with name '{}'", argument.getName());
      }
    }
  }

  void checkField(GraphQLArgument argument, TypeDefinitionRegistry registry, String name, String typeName) {
    Optional<TypeDefinition> optional = registry.getType(typeName);
    if (!optional.isPresent() || ((ObjectTypeDefinition) optional.get()).getFieldDefinitions()
        .stream()
        .noneMatch(fieldDefinition -> fieldDefinition.getName()
            .equals(argument.getValue()))) {
      throw new DirectiveValidationException(
          "SparqlFilter 'field' [{}] on field '{}' is invalid. It does not exist on type '{}'", argument.getValue(),
          name, typeName);
    }
  }

  void checkOperator(GraphQLArgument argument, String name) {
    if (argument.getValue() != null && !argument.getValue()
        .toString()
        .matches("^(=|!=|<|<=|>|>=)$")) {
      throw new DirectiveValidationException(
          "SparqlFilter 'operator' [{}] on field '{}' is invalid. It should be one of: '=', '!=', '<', '<=', '>',"
              + " '>='",
          argument.getValue(), name);
    }
  }

  private Type<?> getBaseType(Type<?> type) {
    if (type instanceof ListType) {
      return getBaseType((Type) type.getChildren()
          .get(0));
    }
    if (type instanceof NonNullType) {
      return getBaseType(((NonNullType) type).getType());
    }
    return type;
  }

}
