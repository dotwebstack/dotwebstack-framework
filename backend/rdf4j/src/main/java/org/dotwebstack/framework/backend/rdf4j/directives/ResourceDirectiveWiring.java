package org.dotwebstack.framework.backend.rdf4j.directives;

import graphql.Scalars;
import graphql.language.NonNullType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.dotwebstack.framework.core.directives.ValidatingDirectiveWiring;
import org.springframework.stereotype.Component;

@Component
public class ResourceDirectiveWiring extends ValidatingDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  @Override
  public String getDirectiveName() {
    return Rdf4jDirectives.RESOURCE_NAME;
  }

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
    GraphQLFieldsContainer fieldsContainer = environment.getFieldsContainer();
    GraphQLFieldDefinition element = environment.getElement();

    validate(getDirectiveName(), fieldDefinition, fieldsContainer, () -> {
      assert GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType())
          .getName()
          .equals(Scalars.GraphQLString.getName()) : "can only be defined on a String field";
      assert element.getDefinition()
          .getType() instanceof NonNullType : "can only be defined on non-nullable field";
    });

    return element;
  }

}
