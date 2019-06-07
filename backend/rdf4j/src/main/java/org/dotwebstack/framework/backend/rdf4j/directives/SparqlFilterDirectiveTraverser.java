package org.dotwebstack.framework.backend.rdf4j.directives;

import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SparqlFilterDirectiveTraverser {

  public Map<GraphQLDirectiveContainer, Object> getDirectiveContainers(DataFetchingEnvironment dataFetchingEnvironment,
      String directiveName) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    return fieldDefinition.getArguments()
        .stream()
        .flatMap(argument -> getInputObjectFieldsFromArgument(argument).stream())
        .filter(directiveContainer -> directiveContainer.getDirective(directiveName) != null)
        .map(directiveContainer -> new AbstractMap.SimpleEntry<>(directiveContainer,
            dataFetchingEnvironment.getArgument(directiveContainer.getName())))
        .filter(entry -> entry.getValue() != null)
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  private List<GraphQLDirectiveContainer> getInputObjectFieldsFromArgument(GraphQLArgument argument) {
    if (argument.getType() instanceof GraphQLInputObjectType) {
      return getInputObjectFieldsFromObjectType((GraphQLInputObjectType) argument.getType());
    } else if ((GraphQLTypeUtil.unwrapAll(argument.getType()) instanceof GraphQLScalarType)) {
      return Collections.singletonList(argument);
    }

    return Collections.emptyList();
  }

  private List<GraphQLDirectiveContainer> getInputObjectFieldsFromObjectType(GraphQLInputObjectType inputObjectType) {
    List<GraphQLDirectiveContainer> directiveContainers = new ArrayList<>();
    // Process nested inputObjectTypes
    directiveContainers.addAll(inputObjectType.getFields()
        .stream()
        .filter(field -> field.getType() instanceof GraphQLInputObjectType)
        .flatMap(field -> getInputObjectFieldsFromObjectType((GraphQLInputObjectType) field.getType()).stream())
        .collect(Collectors.toList()));

    // Process fields on inputObjectType
    directiveContainers.addAll(inputObjectType.getFields()
        .stream()
        .filter(field -> field.getType() instanceof GraphQLScalarType)
        .collect(Collectors.toList()));

    return directiveContainers;
  }

  List<String> getReturnTypes(TypeDefinition<?> baseType, TypeDefinitionRegistry registry) {
    List<String> typeNames = new ArrayList<>();

    registry.types()
        .keySet()
        .forEach(item -> registry.getType(item)
            .ifPresent(compareType -> {
              if (compareType instanceof ObjectTypeDefinition) {
                if (compareType.getDirective(Rdf4jDirectives.SPARQL_NAME) != null) {
                  // These are our query objects
                  typeNames.addAll(processQuery(registry, baseType, (ObjectTypeDefinition) compareType));
                } else {
                  // Regular input object types
                  typeNames.addAll(processObjectType(registry, baseType, compareType));
                }
              } else if (compareType instanceof InputObjectTypeDefinition) {
                typeNames.addAll(processInputObjectType(registry, baseType, compareType));
              }
            }));

    return typeNames;
  }

  private List<String> processQuery(TypeDefinitionRegistry registry, TypeDefinition<?> parentType,
      ObjectTypeDefinition compareType) {
    return compareType.getFieldDefinitions()
        .stream()
        .filter(inputField -> inputField.getInputValueDefinitions()
            .stream()
            .anyMatch(inputValueDefinition -> registry.getType(getBaseType(inputValueDefinition.getType()))
                .map(definition -> definition.equals(parentType))
                .orElse(false)))
        .map(inputField -> ((TypeName) getBaseType(inputField.getType())).getName())
        .collect(Collectors.toList());
  }

  private List<String> processInputObjectType(TypeDefinitionRegistry registry, TypeDefinition<?> baseType,
      TypeDefinition<?> compareType) {

    Optional<InputValueDefinition> inputValueDefinition =
        ((InputObjectTypeDefinition) compareType).getInputValueDefinitions()
            .stream()
            .filter(inputValue -> registry.getType(getBaseType(inputValue.getType()))
                .map(definition -> definition.equals(baseType))
                .orElse(false))
            .findAny();

    if (inputValueDefinition.isPresent()) {
      return getReturnTypes(compareType, registry);
    }
    return Collections.emptyList();
  }

  private List<String> processObjectType(TypeDefinitionRegistry registry, TypeDefinition<?> parentType,
      TypeDefinition<?> compareType) {
    Optional<FieldDefinition> inputValueDefinition = ((ObjectTypeDefinition) compareType).getFieldDefinitions()
        .stream()
        .filter(inputField -> registry.getType(getBaseType(inputField.getType()))
            .map(definition -> definition.equals(parentType))
            .orElse(false))
        .findAny();

    if (inputValueDefinition.isPresent()) {
      return getReturnTypes(compareType, registry);
    }
    return Collections.emptyList();
  }

  private Type<?> getBaseType(Type<?> type) {
    if (type instanceof ListType) {
      return getBaseType((Type<?>) type.getChildren()
          .get(0));
    }
    if (type instanceof NonNullType) {
      return getBaseType(((NonNullType) type).getType());
    }
    return type;
  }
}
