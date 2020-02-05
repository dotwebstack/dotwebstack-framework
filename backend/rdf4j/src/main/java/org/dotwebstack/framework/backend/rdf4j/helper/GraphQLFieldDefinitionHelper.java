package org.dotwebstack.framework.backend.rdf4j.helper;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;

public class GraphQLFieldDefinitionHelper {

  public static final String SELECT_QUERY_COMMAND = "SELECT";

  public static final String DESCRIBE_QUERY_COMMAND = "DESCRIBE";

  public static final String CONSTRUCT_QUERY_COMMAND = "CONSTRUCT";

  public static boolean graphQlFieldDefinitionIsOfType(GraphQLOutputType type, GraphQLScalarType model) {
    return type.getName()
        .equals(model.getName());
  }

  public static void validateQueryHasCommand(String staticSparqlQuery, String... queryOperators) {
    boolean invalid = true;

    for (String queryOperator : queryOperators) {
      if (staticSparqlQuery.startsWith(queryOperator)) {
        invalid = false;
        break;
      }
    }

    if (invalid) {
      throw invalidConfigurationException("Query for return type IRI must be a {} query", queryOperators);
    }
  }
}
