package org.dotwebstack.framework.core.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.language.FieldDefinition;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.TypeHelper;

public class GraphQlFieldBuilder {

  private final TypeDefinitionRegistry registry;

  public GraphQlFieldBuilder(TypeDefinitionRegistry registry) {
    this.registry = registry;
  }

  public GraphQlField toGraphQlField(@NonNull FieldDefinition fieldDefinition,
      Map<String, GraphQlField> typeNameFieldMap) {
    List<GraphQlField> fields = new ArrayList<>();
    List<GraphQlArgument> arguments = getArguments(fieldDefinition);

    String typeName = TypeHelper.getTypeName(TypeHelper.getBaseType(fieldDefinition.getType()));

    GraphQlField result = GraphQlField.builder()
        .name(fieldDefinition.getName())
        .type(typeName)
        .fields(fields)
        .arguments(arguments)
        .build();

    if (registry.getType(TypeHelper.getBaseType(fieldDefinition.getType()))
        .filter(t -> t instanceof ObjectTypeDefinition)
        .isPresent() && !typeNameFieldMap.containsKey(typeName)) {
      typeNameFieldMap.put(typeName, result);
    }
    fields.addAll(getGraphQlFields(fieldDefinition, typeNameFieldMap));

    return result;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private List<GraphQlField> getGraphQlFields(FieldDefinition fieldDefinition,
      Map<String, GraphQlField> typeNameFieldMap) {
    Type type = fieldDefinition.getType();
    Type baseType = TypeHelper.getBaseType(type);
    TypeDefinition typeDefinition = this.registry.getType(baseType)
        .orElseThrow(() -> invalidConfigurationException("Type '{}' not found in the GraphQL schema.", baseType));
    if (typeDefinition instanceof ScalarTypeDefinition) {
      return Collections.emptyList();
    }

    // TODO: Verbeter code om met circulaire dependencies om te gaan

    List<FieldDefinition> children = typeDefinition.getChildren();
    return children.stream()
        .map(childFieldDefinition -> {
          String childType = TypeHelper.getTypeName(TypeHelper.getBaseType(childFieldDefinition.getType()));
          if (typeNameFieldMap.containsKey(childType) && childFieldDefinition.getChildren()
              .size() > 0) {
            return GraphQlField.builder()
                .name(childFieldDefinition.getName())
                .fields(typeNameFieldMap.get(childType)
                    .getFields())
                .arguments(typeNameFieldMap.get(childType)
                    .getArguments())
                .type(typeNameFieldMap.get(childType)
                    .getType())
                .build();
          }
          return toGraphQlField(childFieldDefinition, typeNameFieldMap);
        })
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
    GraphQlArgument.GraphQlArgumentBuilder builder = GraphQlArgument.builder();

    Type inputValueDefinitionType = inputValueDefinition.getType();
    builder.required(inputValueDefinitionType instanceof NonNullType);
    builder.hasDefault(inputValueDefinition.getDefaultValue() != null);

    Type<?> baseType = TypeHelper.getBaseType(inputValueDefinitionType);
    String baseTypeName = TypeHelper.getTypeName(baseType);
    TypeDefinition typeDefinition = this.registry.getType(baseType)
        .orElseThrow(() -> invalidConfigurationException("Type '{}' not found in the GraphQL schema.", baseType));

    builder.name(inputValueDefinition.getName())
        .type(inputValueDefinitionType)
        .baseType(baseTypeName);
    if (typeDefinition instanceof InputObjectTypeDefinition) {
      builder.children(((InputObjectTypeDefinition) typeDefinition).getInputValueDefinitions()
          .stream()
          .map(this::toGraphQlArgument)
          .collect(Collectors.toList()));
    }
    return builder.build();
  }
}
