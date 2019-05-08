package org.dotwebstack.framework.core.directives;

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


  @SuppressWarnings("unchecked")
  private void onArguments(GraphQLArgument argument, Object value) {
    if (!(argument.getType() instanceof GraphQLScalarType)) {
      if (argument.getType() instanceof GraphQLInputObjectType) {
        onInputObjectType((GraphQLInputObjectType) argument.getType(),
                (Map<String,Object>)value);
        return;
      }
    }

    for (GraphQLDirective directive : argument.getDirectives()) {
      getDirectiveValidator(directive).ifPresent(val ->
              val.onArgument(directive,argument,value));
    }
  }

  @SuppressWarnings("unchecked")
  private void onInputObjectType(GraphQLInputObjectType inputObjectType,
                                 Map<String,Object> arguments) {
    inputObjectType.getFields().stream()
            .filter(field -> field.getType() instanceof GraphQLInputObjectType)
            .forEach(field -> onInputObjectType((GraphQLInputObjectType) field.getType(),
                    (Map<String,Object>)arguments.get(field.getName())));

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
    return validators.stream().filter(val -> val.supports(directive.getName()))
            .findFirst();
  }

}
