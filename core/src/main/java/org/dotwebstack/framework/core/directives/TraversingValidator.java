package org.dotwebstack.framework.core.directives;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TraversingValidator {

  // GET THE OBJECT TYPES THAT CAN BE RECHED FROM A GIVEN ENVIRONMENT
  public Map<GraphQLDirectiveContainer, Object> getSelectionFieldArguments(
      DataFetchingEnvironment environment, String directive) {
    return environment.getSelectionSet()
        .getFields()
        .stream()
        .filter(selectedField -> selectedField.getArguments()
            .size() > 0)
        .flatMap(selectedField -> selectedField.getFieldDefinition()
            .getArguments()
            .stream()
            .filter(argument -> argument.getDirective(directive) != null)
            .map(argumentDefinition -> new AbstractMap.SimpleEntry<>(argumentDefinition, selectedField.getArguments()
                .get(argumentDefinition.getName()))))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  // GET INPUT OBJECT TYPES THAT CAN BE REACHED FROM GIVEN ARGUMENT
  public List<GraphQLDirectiveContainer> getInputObjectFieldsFromArgument(GraphQLArgument argument) {
    if (argument.getType() instanceof GraphQLInputObjectType) {
      return getInputObjectFieldsFromObjectType((GraphQLInputObjectType) argument.getType());
    } else if ((GraphQLTypeUtil.unwrapAll(argument.getType()) instanceof GraphQLScalarType)) {
      return Collections.singletonList(argument);
    }

    return Collections.emptyList();
  }

  // GET INPUT OBJECT TYPES THAT CAN BE REACHED FROM A GIVEN INPUT OBJECT TYPE
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

}
