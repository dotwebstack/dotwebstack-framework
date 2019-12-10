package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.language.NonNullType;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.Collection;
import java.util.List;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
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

    try {
      validateOnlyOnIri(GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType()));
      validateOnlyOncePerType(fieldsContainer.getFieldDefinitions());
      validateOnlyRequired(element);

    } catch (ValidationException vex) {
      String typeName = fieldsContainer.getName();
      String fieldName = fieldDefinition.getName();

      throw invalidConfigurationException("[GraphQL] Found an error on @{} directive defined on {}.{}: {} ",
          DIRECTIVE_NAME, typeName, fieldName, vex);
    }

    return element;
  }

  private void validateOnlyOnIri(GraphQLType rawType) throws ValidationException {
    if (!(rawType.getName()
        .equals(Rdf4jScalars.IRI.getName()))) {
      throw new ValidationException("can only be defined on a IRI field");
    }
  }

  private void validateOnlyOncePerType(List<GraphQLFieldDefinition> fields) throws ValidationException {
    long directiveCount = fields.stream()
        .map(GraphQLFieldDefinition::getDirectives)
        .flatMap(Collection::stream)
        .map(GraphQLDirective::getName)
        .filter(DIRECTIVE_NAME::equals)
        .count();
    if (directiveCount > 1) {
      throw new ValidationException("can only be defined once per type");
    }
  }

  private void validateOnlyRequired(GraphQLFieldDefinition argument) throws ValidationException {
    if (!(argument.getDefinition()
        .getType() instanceof NonNullType)) {
      throw new ValidationException("can only be defined on non-nullable field");
    }
  }

  private static class ValidationException extends Throwable {
    ValidationException(String message) {
      super(message);
    }
  }
}
