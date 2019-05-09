package org.dotwebstack.framework.core.directives;

import static org.dotwebstack.framework.core.helpers.ObjectHelper.cast;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLScalarType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DirectiveValidatorDelegator {

  private final List<DirectiveValidator> validators;

  public void delegate(DataFetchingEnvironment dataFetchingEnvironment) {
    GraphQLFieldDefinition fieldDefinition = dataFetchingEnvironment.getFieldDefinition();
    Map<String,Object> arguments = dataFetchingEnvironment.getArguments();

    fieldDefinition.getArguments().forEach(argument ->
            onArguments(argument, arguments.get(argument.getName())));
  }


  private void onArguments(GraphQLArgument argument, Object value) {
    if (argument.getType() instanceof GraphQLInputObjectType) {

      onInputObjectType((GraphQLInputObjectType) argument.getType(), castToMap(value));
      return;
    }

    if (!(argument.getType() instanceof GraphQLScalarType)) {
      // only call validator on argument method for scalar types!
      return;
    }

    for (GraphQLDirective directive : argument.getDirectives()) {
      getDirectiveValidator(directive).ifPresent(val -> val.onArgument(directive,argument,value));
    }
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
            .forEach(field -> {
              for (GraphQLDirective directive : field.getDirectives()) {
                getDirectiveValidator(directive).ifPresent(val ->
                        val.onInputObjectField(directive,field,
                                arguments.get(field.getName())));
              }
            });
  }

  private Optional<DirectiveValidator> getDirectiveValidator(GraphQLDirective directive) {
    return validators.stream().filter(val -> val.supports(directive.getName())).findFirst();
  }

}
