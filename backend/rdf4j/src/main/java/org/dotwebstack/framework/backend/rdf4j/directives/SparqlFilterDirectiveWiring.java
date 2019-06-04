package org.dotwebstack.framework.backend.rdf4j.directives;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphqlElementParentTree;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SparqlFilterDirectiveWiring implements SchemaDirectiveWiring {

  private SparqlFilterValidator validator;

  @Override
  public GraphQLArgument onArgument(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    try {
      environment.getElementParentTree()
          .getParentInfo()
          .ifPresent(parentInfo -> {
            GraphqlElementParentTree grandParentInfo = parentInfo.getParentInfo()
                .orElseThrow(() -> ExceptionHelper.illegalArgumentException("Did not find a parent type for `{}`",
                    environment.getElement()));

            if (!grandParentInfo.getElement()
                .getName()
                .equals("Query")) {
              throw ExceptionHelper.illegalArgumentException("'{}' can only be used as an argument for a Query!",
                  environment.getElement());
            }

            GraphQLObjectType type = (GraphQLObjectType) GraphQLTypeUtil
                .unwrapAll(((GraphQLFieldDefinition) parentInfo.getElement()).getType());
            validator.validate(environment.getElement(), type.getName());
          });
    } catch (DirectiveValidationException exception) {
      throwConfigurationException(exception);
    }
    return environment.getElement();
  }

  @Override
  public GraphQLInputObjectField onInputObjectField(
      SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    try {
      TypeDefinitionRegistry registry = environment.getRegistry();

      environment.getElementParentTree()
          .getParentInfo()
          .ifPresent(parentInfo -> {
            TypeDefinition<?> typeDefinition = registry.types()
                .get(parentInfo.getElement()
                    .getName());
            traverse(environment.getElement(), typeDefinition, registry);
          });

    } catch (DirectiveValidationException exception) {
      throwConfigurationException(exception);
    }
    return environment.getElement();
  }

  private void traverse(GraphQLDirectiveContainer container, TypeDefinition<?> baseType,
      TypeDefinitionRegistry registry) {
    registry.types()
        .keySet()
        .forEach(item -> registry.getType(item)
            .ifPresent(compareType -> {
              if (compareType instanceof ObjectTypeDefinition) {
                if (item.equals("Query")) {
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
        .filter(inputField -> registry.getType(getBaseType(inputField.getType()))
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
        .forEach(inputField -> validator.validate(container, ((TypeName) getBaseType(inputField.getType())).getName()));
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

  private void throwConfigurationException(Exception cause) {
    throw new InvalidConfigurationException("Default value in constraint directive is violating constraint!", cause);
  }
}
