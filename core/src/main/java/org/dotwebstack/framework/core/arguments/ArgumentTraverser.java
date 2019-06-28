package org.dotwebstack.framework.core.arguments;

import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLType;
import java.util.Map;
import lombok.NonNull;

public abstract class ArgumentTraverser {

  public void traverse(@NonNull DataFetchingEnvironment dataFetchingEnvironment) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String, Object> arguments = dataFetchingEnvironment.getArguments();

    fieldDefinition.getArguments()
        .stream()
        .forEach(argument -> {
          Object value = arguments.get(argument.getName());
          if (argument.getType() instanceof GraphQLInputObjectType) {
            traverseInputObjectType(fieldDefinition.getType(), (GraphQLInputObjectType) argument.getType(),
                value != null ? castToMap(value) : ImmutableMap.of());
          } else {
            traverseArgument(fieldDefinition.getType(), argument, value);
          }
        });
  }

  protected abstract void onInputObjectField(GraphQLType fieldDefinitionType, GraphQLInputObjectField inputObjectField,
      Object value);

  protected abstract void onArgument(GraphQLType fieldDefinitionType, GraphQLArgument argument, Object value);

  private void traverseArgument(GraphQLType fieldDefinitionType, GraphQLArgument argument, Object value) {
    onArgument(fieldDefinitionType, argument, value);
    if (argument.getType() instanceof GraphQLInputObjectType) {
      traverseInputObjectType(fieldDefinitionType, (GraphQLInputObjectType) argument.getType(),
          value != null ? castToMap(value) : ImmutableMap.of());
    }
  }

  private void traverseInputObjectType(GraphQLType fieldDefinitionType, GraphQLInputObjectType graphQlInputObjectType,
      Map<String, Object> value) {
    graphQlInputObjectType.getFields()
        .forEach(fd -> {
          traverseInputObjectField(fieldDefinitionType, fd, value.get(fd.getName()));
        });
  }

  private void traverseInputObjectField(GraphQLType fieldDefinitionType, GraphQLInputObjectField inputObjectField,
      Object value) {
    onInputObjectField(fieldDefinitionType, inputObjectField, value);
    if (inputObjectField.getType() instanceof GraphQLInputObjectType) {
      traverseInputObjectType(fieldDefinitionType, (GraphQLInputObjectType) inputObjectField.getType(),
          value != null ? castToMap(value) : ImmutableMap.of());
    }
  }
}
