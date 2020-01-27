package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.language.NonNullType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.springframework.stereotype.Component;

@Component
public class ResourceDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  @Override
  public String getDirectiveName() {
    return Rdf4jDirectives.RESOURCE_NAME;
  }

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
    GraphQLFieldsContainer fieldsContainer = environment.getFieldsContainer();
    GraphQLFieldDefinition element = environment.getElement();

    String typeName = fieldsContainer.getName();
    String fieldName = fieldDefinition.getName();
    validateOnlyOnIri(typeName, fieldName, GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType()));
    validateOnlyRequired(typeName, fieldName, element);

    return element;
  }

  private void validateOnlyOnIri(String typeName, String fieldName, GraphQLType rawType) {
    if (!(rawType.getName()
        .equals(Rdf4jScalars.IRI.getName()))) {
      throw invalidConfigurationException("{}.{} should be of type IRI for @resource directive", typeName, fieldName);
    }
  }

  private void validateOnlyRequired(String typeName, String fieldName, GraphQLFieldDefinition argument) {
    if (!(argument.getDefinition()
        .getType() instanceof NonNullType)) {
      throw invalidConfigurationException("{}.{} should be an non-nullable field for @resource directive", typeName,
          fieldName);
    }
  }
}
