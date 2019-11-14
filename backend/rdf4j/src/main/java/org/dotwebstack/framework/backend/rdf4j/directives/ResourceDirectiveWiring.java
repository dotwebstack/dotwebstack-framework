package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.language.NonNullType;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Collection;
import java.util.List;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.springframework.stereotype.Component;

@Component
public class ResourceDirectiveWiring implements SchemaDirectiveWiring {

  private static final String DIRECTIVE_NAME = Rdf4jDirectives.RESOURCE_NAME;

  public ResourceDirectiveWiring() {}

  @Override
  public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
    String fieldName = fieldDefinition.getName();

    GraphQLFieldsContainer fieldsContainer = environment.getFieldsContainer();
    String typeName = fieldsContainer.getName();

    validateOnlyOnIri(typeName, fieldName, GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType()));

    validateOnlyOncePerType(typeName, fieldName, fieldsContainer.getFieldDefinitions());

    validateOnlyRequired(typeName, fieldName, environment.getElement());

    return environment.getElement();
  }

  private void validateOnlyOnIri(String typename, String fieldname, GraphQLType rawType) {
    if (!(rawType.getName()
        .equals(Rdf4jScalars.IRI.getName()))) {
      invalidConfigurationFor(typename, fieldname, "can only be defined on a IRI field");
    }
  }

  private void validateOnlyOncePerType(String typeName, String fieldName, List<GraphQLFieldDefinition> fields) {
    long directiveCount = fields.stream()
        .map(GraphQLFieldDefinition::getDirectives)
        .flatMap(Collection::stream)
        .map(GraphQLDirective::getName)
        .filter(DIRECTIVE_NAME::equals)
        .count();
    if (directiveCount > 1) {
      invalidConfigurationFor(typeName, fieldName, "can only be defined once per type");
    }
  }

  private void validateOnlyRequired(String typeName, String fieldName, GraphQLFieldDefinition argument) {
    if (!(argument.getDefinition()
        .getType() instanceof NonNullType)) {
      invalidConfigurationFor(typeName, fieldName, "can only be defined on non-nullable field");
    }
  }

  private void invalidConfigurationFor(String typeName, String fieldName, String reason) {
    throw invalidConfigurationException("[GraphQL] Found an error on @{} directive defined on {}.{}: {} ",
        DIRECTIVE_NAME, typeName, fieldName, reason);
  }
}
