package org.dotwebstack.framework.core.directives;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.springframework.stereotype.Component;

@Component
public final class TransformDirectiveWiring extends ValidatingDirectiveWiring
    implements AutoRegisteredSchemaDirectiveWiring {

  @Override
  public String getDirectiveName() {
    return CoreDirectives.TRANSFORM_NAME;
  }

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition element = environment.getElement();

    validate(getDirectiveName(), element, environment.getFieldsContainer(), () -> validateTypeIsScalar(element));

    return element;
  }

  private void validateTypeIsScalar(GraphQLFieldDefinition fieldDefinition) {
    GraphQLUnmodifiedType type = GraphQLTypeUtil.unwrapAll(fieldDefinition.getType());
    assert GraphQLTypeUtil.isScalar(type) : "can only be used with (a list of) scalar fields.";
  }
}
