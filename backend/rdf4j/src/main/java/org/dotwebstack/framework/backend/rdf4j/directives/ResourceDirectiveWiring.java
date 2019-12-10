package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.Scalars;
import graphql.language.NonNullType;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Collection;
import java.util.List;
import org.dotwebstack.framework.core.directives.AutoRegisteredSchemaDirectiveWiring;
import org.springframework.stereotype.Component;

@Component
public class ResourceDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  private static final String DIRECTIVE_NAME = Rdf4jDirectives.RESOURCE_NAME;

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
    validateOnlyOnString(typeName, fieldName, GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType()));
    validateOnlyOncePerType(typeName, fieldName, fieldsContainer.getFieldDefinitions());
    validateOnlyRequired(typeName, fieldName, element);

    return element;
  }

  private void validateOnlyOnString(String typeName, String fieldName, GraphQLType rawType) {
    if (!(rawType.getName()
        .equals(Scalars.GraphQLString.getName()))) {
      throw invalidConfigurationException("{}.{} should be of type String for @resource directive", typeName,
          fieldName);
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
      throw invalidConfigurationException("{}.{} should have only one @resource directive", typeName, fieldName);
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
