package org.dotwebstack.framework.core.directives;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
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
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.helpers.FilterHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FilterDirectiveTraverser {

  public Map<GraphQLDirectiveContainer, Object> getInputObjectDirectiveContainers(
      DataFetchingEnvironment dataFetchingEnvironment, String directiveName) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();

    getObjectDirectiveContainers((GraphQLObjectType) GraphQLTypeUtil.unwrapAll(fieldDefinition.getType()));

    Map<String, Object> flattenedArguments = dataFetchingEnvironment.getArguments()
        .entrySet()
        .stream()
        .flatMap(entry -> FilterHelper.flatten(entry)
            .entrySet()
            .stream())
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);

    return fieldDefinition.getArguments()
        .stream()
        .flatMap(argument -> getInputObjectFieldsFromArgument(argument).stream())
        .filter(directiveContainer -> directiveContainer.getDirective(directiveName) != null)
        .map(directiveContainer -> new AbstractMap.SimpleEntry<>(directiveContainer,
            flattenedArguments.get(directiveContainer.getName())))
        .filter(entry -> entry.getValue() != null)
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  private Map<GraphQLDirectiveContainer, Object> getObjectDirectiveContainers(GraphQLObjectType graphQLObjectType) {
    // 1. get the filters from current field
    graphQLObjectType.getFieldDefinitions()
        .stream()
        .filter(fieldDefinition -> fieldDefinition.getDirective(CoreDirectives.FILTER_NAME) != null)
        .collect(Collectors.toList());

    // 2. get the object types to repeat the process
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
        .filter(field -> GraphQLTypeUtil.unwrapAll(field.getType()) instanceof GraphQLScalarType)
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
                typeNames.addAll(processQuery(registry, baseType, (ObjectTypeDefinition) compareType));
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
            .anyMatch(inputValueDefinition -> registry.getType(FilterHelper.getBaseType(inputValueDefinition.getType()))
                .map(definition -> definition.equals(parentType))
                .orElse(false)))
        .map(inputField -> ((TypeName) FilterHelper.getBaseType(inputField.getType())).getName())
        .collect(Collectors.toList());
  }

  private List<String> processInputObjectType(TypeDefinitionRegistry registry, TypeDefinition<?> baseType,
      TypeDefinition<?> compareType) {

    Optional<InputValueDefinition> inputValueDefinition =
        ((InputObjectTypeDefinition) compareType).getInputValueDefinitions()
            .stream()
            .filter(inputValue -> registry.getType(FilterHelper.getBaseType(inputValue.getType()))
                .map(definition -> definition.equals(baseType))
                .orElse(false))
            .findAny();

    if (inputValueDefinition.isPresent()) {
      return getReturnTypes(compareType, registry);
    }
    return Collections.emptyList();
  }
}
