package org.dotwebstack.framework.core.directives;

import graphql.language.FieldDefinition;
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
public class FilterDirectiveTraverser extends TraversingValidator {

  public Map<GraphQLDirectiveContainer, Object> getInputObjectDirectiveContainers(
      DataFetchingEnvironment dataFetchingEnvironment) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String, Object> flattenedArguments = FilterHelper.flattenArguments(dataFetchingEnvironment.getArguments());

    return fieldDefinition.getArguments()
        .stream()
        .flatMap(argument -> getInputObjectFieldsFromArgument(argument).stream())
        .filter(directiveContainer -> directiveContainer.getDirective(CoreDirectives.FILTER_NAME) != null)
        .map(directiveContainer -> new AbstractMap.SimpleEntry<>(directiveContainer,
            flattenedArguments.get(directiveContainer.getName())))
        .filter(entry -> entry.getValue() != null)
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  public Map<GraphQLDirectiveContainer, Object> getObjectDirectiveContainers(
      DataFetchingEnvironment dataFetchingEnvironment) {
    return getSelectionFieldArguments(dataFetchingEnvironment, CoreDirectives.FILTER_NAME);
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
