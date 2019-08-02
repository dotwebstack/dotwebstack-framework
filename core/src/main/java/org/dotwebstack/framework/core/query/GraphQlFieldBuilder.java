package org.dotwebstack.framework.core.query;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.helpers.TypeHelper;

public class GraphQlFieldBuilder {

  private final TypeDefinitionRegistry registry;

  public GraphQlFieldBuilder(TypeDefinitionRegistry registry) {
    this.registry = registry;
  }

  public GraphQlField toGraphQlField(@NonNull FieldDefinition fieldDefinition) {
    List<GraphQlField> fields = getGraphQlFields(fieldDefinition);
    List<GraphQlArgument> arguments = getArguments(fieldDefinition);
    return GraphQlField.builder()
        .name(fieldDefinition.getName())
        .type(TypeHelper.getTypeName(TypeHelper.getBaseType(fieldDefinition.getType())))
        .fields(fields)
        .arguments(arguments)
        .build();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private List<GraphQlField> getGraphQlFields(FieldDefinition fieldDefinition) {
    Type type = fieldDefinition.getType();
    Type baseType = TypeHelper.getBaseType(type);
    TypeDefinition typeDefinition = this.registry.getType(baseType)
        .orElseThrow(() -> ExceptionHelper.invalidConfigurationException("Type '{}' not found in the GraphQL schema.",
            baseType));
    if (typeDefinition instanceof ScalarTypeDefinition) {
      return Collections.emptyList();
    }
    List<FieldDefinition> children = typeDefinition.getChildren();
    return children.stream()
        .map(this::toGraphQlField)
        .collect(Collectors.toList());
  }

  private List<GraphQlArgument> getArguments(FieldDefinition fieldDefinition) {
    return fieldDefinition.getInputValueDefinitions()
        .stream()
        .map(this::toGraphQlArgument)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("rawtypes")
  private GraphQlArgument toGraphQlArgument(InputValueDefinition inputValueDefinition) {
    Type<?> baseType = TypeHelper.getBaseType(inputValueDefinition.getType());
    String type = TypeHelper.getTypeName(baseType);
    TypeDefinition typeDefinition = this.registry.getType(baseType)
        .orElseThrow(() -> ExceptionHelper.invalidConfigurationException("Type '{}' not found in the GraphQL schema.",
            baseType));

    return new GraphQlArgument(inputValueDefinition.getName(), type, null);
  }
}
