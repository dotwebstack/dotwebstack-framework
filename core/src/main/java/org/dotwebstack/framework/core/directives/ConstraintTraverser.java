package org.dotwebstack.framework.core.directives;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLScalarType;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConstraintTraverser {

  private final ConstraintValidator validator;

  public void traverse(DataFetchingEnvironment dataFetchingEnvironment) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String,Object> arguments = dataFetchingEnvironment.getArguments();

    fieldDefinition.getArguments().forEach(argument ->
        onArguments(argument, arguments.get(argument.getName())));
  }


  void onArguments(GraphQLArgument argument, Object value) {
    if (argument.getType() instanceof GraphQLInputObjectType) {

      onInputObjectType((GraphQLInputObjectType) argument.getType(),
          value != null ? castToMap(value) : ImmutableMap.of());
      return;
    }

    if (!(argument.getType() instanceof GraphQLScalarType)) {
      // only call validator on argument method for scalar types!
      return;
    }

    validate(argument,argument.getName(),ofNullable(value).orElse(argument.getDefaultValue()));
  }

  private void onInputObjectType(GraphQLInputObjectType inputObjectType,
                                 Map<String,Object> arguments) {

    // Process nested inputObjectTypes
    inputObjectType.getFields().stream()
        .filter(field -> field.getType() instanceof GraphQLInputObjectType)
        .forEach(field -> onInputObjectType((GraphQLInputObjectType) field.getType(),
            castToMap(arguments.get(field.getName()))));

    // Process fields on inputObjectType
    inputObjectType.getFields().stream()
        .filter(field -> field.getType() instanceof GraphQLScalarType)
        .forEach(field -> onInputObjectField(field,arguments.get(field.getName())));
  }

  void onInputObjectField(GraphQLInputObjectField inputObjectField, Object value) {
    validate(inputObjectField,inputObjectField.getName(),
        ofNullable(value).orElse(inputObjectField.getDefaultValue()));
  }

  private void validate(GraphQLDirectiveContainer directiveContainer, String name, Object value) {
    ofNullable(directiveContainer.getDirective(CoreDirectives.CONSTRAINT_NAME))
        .map(GraphQLDirective::getArguments).ifPresent(directiveArguments ->
          directiveArguments.forEach(directiveArgument ->
              validator.validate(directiveArgument, name,value)));
  }
}
