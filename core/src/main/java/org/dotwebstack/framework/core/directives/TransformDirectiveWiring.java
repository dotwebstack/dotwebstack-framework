package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.springframework.stereotype.Component;

@Component
public final class TransformDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  @Override
  public String getDirectiveName() {
    return CoreDirectives.TRANSFORM_NAME;
  }

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getElement();

    if (!GraphQLTypeUtil.isScalar(GraphQLTypeUtil.unwrapAll(fieldDefinition.getType()))) {
      throw new InvalidConfigurationException("Directive @transform can only be used with (a list of) scalar fields.");
    }

    return fieldDefinition;
  }
}
