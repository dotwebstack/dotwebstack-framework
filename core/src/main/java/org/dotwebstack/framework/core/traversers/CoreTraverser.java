package org.dotwebstack.framework.core.traversers;

import static java.util.Collections.emptyList;

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
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;


@Component
public class CoreTraverser {

  /*
   * Return the directive containers in a given environment for the given directive that are attached
   * to input object types
   */
  public Map<GraphQLDirectiveContainer, Object> getInputObjectDirectiveContainers(
      DataFetchingEnvironment dataFetchingEnvironment, String directiveName) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String, Object> flattenedArguments = TraverserHelper.flattenArguments(dataFetchingEnvironment.getArguments());

    return fieldDefinition.getArguments()
        .stream()
        .flatMap(argument -> getInputObjectFieldsFromArgument(argument).stream())
        .filter(directiveContainer -> directiveContainer.getDirective(directiveName) != null)
        .map(directiveContainer -> new AbstractMap.SimpleEntry<>(directiveContainer,
            flattenedArguments.get(directiveContainer.getName())))
        .filter(entry -> entry.getValue() != null)
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  /*
   * return a map containing the object types that can be reached top down from a given environment
   * together with the argument for this object type, provided by the user.
   */
  public Map<GraphQLDirectiveContainer, Object> getObjectTypes(DataFetchingEnvironment environment) {
    return environment.getSelectionSet()
        .getFields()
        .stream()
        .filter(selectedField -> selectedField.getArguments()
            .size() > 0)
        .flatMap(selectedField -> selectedField.getFieldDefinition()
            .getArguments()
            .stream()
            .map(argumentDefinition -> new AbstractMap.SimpleEntry<>(argumentDefinition, selectedField.getArguments()
                .get(argumentDefinition.getName()))))
        .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
  }

  /*
   * return the list containing the input object types walked bottom up to reach the first object type
   * from a given input object type
   */
  public List<String> getPathToQuery(TypeDefinition<?> baseType, TypeDefinitionRegistry registry) {
    return registry.types()
        .keySet()
        .stream()
        .map(registry::getType)
        .map(Optional::get)
        .flatMap(typeDefinition -> {
          if (typeDefinition instanceof ObjectTypeDefinition) {
            return traverseObjectType(registry, baseType, (ObjectTypeDefinition) typeDefinition).stream();
          } else if (typeDefinition instanceof InputObjectTypeDefinition) {
            return traverseInputObjectType(registry, baseType, typeDefinition).stream();
          }
          return Stream.empty();
        })
        .collect(Collectors.toList());
  }


  /*
   * return a list containing the input object types that can be reached top down from a given
   * argument
   */
  private List<GraphQLDirectiveContainer> getInputObjectFieldsFromArgument(GraphQLArgument argument) {
    if (argument.getType() instanceof GraphQLInputObjectType) {
      return getInputObjectFieldsFromObjectType((GraphQLInputObjectType) argument.getType());
    } else if ((GraphQLTypeUtil.unwrapAll(argument.getType()) instanceof GraphQLScalarType)) {
      return Collections.singletonList(argument);
    }

    return emptyList();
  }

  /*
   * return a list containing the input object types that can be reached top down from a given input
   * object type
   */
  private List<GraphQLDirectiveContainer> getInputObjectFieldsFromObjectType(GraphQLInputObjectType inputObjectType) {
    return inputObjectType.getFields()
        .stream()
        .flatMap(field -> {
          if (field.getType() instanceof GraphQLInputObjectType) {
            return getInputObjectFieldsFromObjectType((GraphQLInputObjectType) field.getType()).stream();
          } else if (GraphQLTypeUtil.unwrapAll(field.getType()) instanceof GraphQLScalarType) {
            return Stream.of(field);
          }
          return Stream.empty();
        })
        .collect(Collectors.toList());
  }

  /*
   * return the list containing the input object types from the given parent type that match the
   * compare type
   */
  private List<String> traverseObjectType(TypeDefinitionRegistry registry, TypeDefinition<?> compareType,
      ObjectTypeDefinition parentType) {
    return parentType.getFieldDefinitions()
        .stream()
        .filter(inputField -> inputField.getInputValueDefinitions()
            .stream()
            .anyMatch(
                inputValueDefinition -> registry.getType(TraverserHelper.getBaseType(inputValueDefinition.getType()))
                    .map(definition -> definition.equals(compareType))
                    .orElse(false)))
        .map(inputField -> ((TypeName) TraverserHelper.getBaseType(inputField.getType())).getName())
        .collect(Collectors.toList());
  }

  /*
   * return the list containing the input object types from the given parent type (recursive) that
   * match the compare type
   */
  private List<String> traverseInputObjectType(TypeDefinitionRegistry registry, TypeDefinition<?> compareType,
      TypeDefinition<?> parentType) {

    Optional<InputValueDefinition> inputValueDefinition =
        ((InputObjectTypeDefinition) parentType).getInputValueDefinitions()
            .stream()
            .filter(inputValue -> registry.getType(TraverserHelper.getBaseType(inputValue.getType()))
                .map(definition -> definition.equals(compareType))
                .orElse(false))
            .findAny();

    if (inputValueDefinition.isPresent()) {
      return getPathToQuery(parentType, registry);
    }
    return emptyList();
  }

}
