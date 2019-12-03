package org.dotwebstack.framework.core.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class AggregateDirectiveWiring implements SchemaAutoRegisteredDirectiveWiring {

  @Override
  public String getDirectiveName() {
    return CoreDirectives.AGGREGATE_NAME;
  }

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    validate(environment.getFieldsContainer()
        .getName(), environment.getFieldDefinition());
    return environment.getElement();
  }

  @Override
  public GraphQLInputObjectField onInputObjectField(
      SchemaDirectiveWiringEnvironment<GraphQLInputObjectField> environment) {
    validate(environment.getFieldsContainer()
        .getName(), environment.getFieldDefinition());

    return environment.getElement();
  }

  private void validate(String typeName, GraphQLFieldDefinition fieldDefinition) {
    GraphQLType rawType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    boolean hasTransformDirective = Objects.isNull(fieldDefinition.getDirective(CoreDirectives.TRANSFORM_NAME));

    if (hasTransformDirective && !Scalars.GraphQLInt.equals(rawType)) {
      throw invalidConfigurationException(
          "Found an error on @aggregate directive defined on field {}.{}: expected output type is Int but got {}",
          typeName, fieldDefinition.getName(), rawType.getName());
    }
  }
}
