package org.dotwebstack.framework.core.query;

import graphql.language.FieldDefinition;
import graphql.language.ScalarTypeDefinition;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.springframework.stereotype.Component;

@Component
public class GraphQlQueryBuilder {
  private TypeDefinitionRegistry registry;

  public GraphQlQueryBuilder(TypeDefinitionRegistry registry) {
    this.registry = registry;
  }

  public String toQuery(FieldDefinition fieldDefinition) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("{");
    addToQuery(toGraphQlField(fieldDefinition), stringBuilder);
    stringBuilder.append("}");
    return stringBuilder.toString();
  }

  private void addToQuery(GraphQlField field, StringBuilder stringBuilder) {
    stringBuilder.append(field.getName());
    if (!field.getFields()
        .isEmpty()) {
      stringBuilder.append("{");
      String separator = "";
      for (GraphQlField childField : field.getFields()) {
        stringBuilder.append(separator);
        addToQuery(childField, stringBuilder);
        separator = ",";
      }
      stringBuilder.append("}");
    }
  }

  private GraphQlField toGraphQlField(FieldDefinition fieldDefinition) {
    List<GraphQlArgument> arguments = getArguments(fieldDefinition);
    List<GraphQlField> fields = getGraphQlFields(fieldDefinition);
    return GraphQlField.builder()
        .name(fieldDefinition.getName())
        .arguments(arguments)
        .fields(fields)
        .build();
  }

  private List<GraphQlArgument> getArguments(FieldDefinition fieldDefinition) {
    throw ExceptionHelper.unsupportedOperationException("getArguments not yet supported.");
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private List<GraphQlField> getGraphQlFields(FieldDefinition fieldDefinition) {
    Type type = fieldDefinition.getType();
    Type baseType = TypeHelper.getBaseType(type);
    TypeDefinition typeDefinition = this.registry.getType(baseType)
        .orElseThrow(() -> ExceptionHelper.invalidConfigurationException("Type '{}' not found in the GrahpQL schema.",
            baseType));
    if (typeDefinition instanceof ScalarTypeDefinition) {
      return Collections.emptyList();
    }
    List<FieldDefinition> children = typeDefinition.getChildren();
    return children.stream()
        .map(this::toGraphQlField)
        .collect(Collectors.toList());
  }
}
