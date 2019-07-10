package org.dotwebstack.framework.core.traversers;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.springframework.stereotype.Component;

@Component
public class CoreTraverser {

  public List<DirectiveContainerTuple> getTuples(@NonNull DataFetchingEnvironment environment,
      @NonNull TraverserFilter filter) {
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();

    return fieldDefinition.getArguments()
        .stream()
        .flatMap(argument -> getInputObjectFieldsFromArgument(argument, environment.getArguments()).stream())
        .filter(filter::apply)
        .collect(Collectors.toList());
  }

  public List<TypeName> getPathToQuery(@NonNull TypeDefinition<?> baseType, @NonNull TypeDefinitionRegistry registry) {
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


  private List<DirectiveContainerTuple> getInputObjectFieldsFromArgument(GraphQLArgument container,
      Map<String, Object> arguments) {
    if (container.getType() instanceof GraphQLInputObjectType) {
      return getInputObjectFieldsFromObjectType((GraphQLInputObjectType) container.getType(),
          nestedMap(arguments, container.getName()));
    } else if ((GraphQLTypeUtil.unwrapAll(container.getType()) instanceof GraphQLScalarType)) {
      return singletonList(new DirectiveContainerTuple(container, arguments.getOrDefault(container.getName(), null)));
    }

    return emptyList();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> nestedMap(Map<String, Object> arguments, String name) {
    Map<String, Object> result = new HashMap<>();
    if (arguments.containsKey(name) && arguments.get(name) instanceof Map) {
      result.putAll((Map<String, Object>) arguments.get(name));
    }
    return result;
  }

  /*
   * return a list containing the input object types that can be reached top down from a given input
   * object type
   */
  private List<DirectiveContainerTuple> getInputObjectFieldsFromObjectType(GraphQLInputObjectType inputObjectType,
      Map<String, Object> arguments) {
    return inputObjectType.getFields()
        .stream()
        .flatMap(field -> {
          if (field.getType() instanceof GraphQLInputObjectType) {
            return getInputObjectFieldsFromObjectType((GraphQLInputObjectType) field.getType(),
                nestedMap(arguments, field.getName())).stream();
          } else if (GraphQLTypeUtil.unwrapAll(field.getType()) instanceof GraphQLScalarType) {
            return Stream.of(new DirectiveContainerTuple(field, arguments.getOrDefault(field.getName(), null)));
          }
          return Stream.empty();
        })
        .collect(Collectors.toList());
  }

  /*
   * return the list containing the input object types from the given parent type that match the
   * compare type
   */
  private List<TypeName> traverseObjectType(TypeDefinitionRegistry registry, TypeDefinition<?> compareType,
      ObjectTypeDefinition parentType) {
    return parentType.getFieldDefinitions()
        .stream()
        .filter(inputField -> inputField.getInputValueDefinitions()
            .stream()
            .anyMatch(inputValueDefinition -> registry.getType(TypeHelper.getBaseType(inputValueDefinition.getType()))
                .map(definition -> definition.equals(compareType))
                .orElse(false)))
        .map(inputField -> ((TypeName) TypeHelper.getBaseType(inputField.getType())))
        .collect(Collectors.toList());
  }

  /*
   * return the list containing the input object types from the given parent type (recursive) that
   * match the compare type
   */
  private List<TypeName> traverseInputObjectType(TypeDefinitionRegistry registry, TypeDefinition<?> compareType,
      TypeDefinition<?> parentType) {
    Optional<InputValueDefinition> inputValueDefinition =
        ((InputObjectTypeDefinition) parentType).getInputValueDefinitions()
            .stream()
            .filter(inputValue -> registry.getType(TypeHelper.getBaseType(inputValue.getType()))
                .map(definition -> definition.equals(compareType))
                .orElse(false))
            .findAny();

    if (inputValueDefinition.isPresent()) {
      return getPathToQuery(parentType, registry);
    }
    return emptyList();
  }

}
