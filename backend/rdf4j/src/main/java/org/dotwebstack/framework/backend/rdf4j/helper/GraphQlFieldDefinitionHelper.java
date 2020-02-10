package org.dotwebstack.framework.backend.rdf4j.helper;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.eclipse.rdf4j.query.QueryLanguage.SPARQL;

import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;

public class GraphQlFieldDefinitionHelper {

  public static final String SELECT_QUERY_COMMAND = "SELECT";

  public static final String DESCRIBE_QUERY_COMMAND = "DESCRIBE";

  public static final String CONSTRUCT_QUERY_COMMAND = "CONSTRUCT";

  private GraphQlFieldDefinitionHelper() {
    throw unsupportedOperationException("Constructor should not be used");
  }

  public static boolean graphQlFieldDefinitionIsOfType(@NonNull GraphQLOutputType type,
      @NonNull GraphQLScalarType model) {
    return type.getName()
        .equals(model.getName());
  }

  public static void validateQueryHasCommand(String staticSparqlQuery, String... queryOperators) {
    boolean invalid = true;

    ParsedQuery parsedQuery = QueryParserUtil.parseQuery(SPARQL, staticSparqlQuery, null);
    List<String> operators = Arrays.asList(queryOperators);

    if (parsedQuery instanceof ParsedGraphQuery) {
      if (operators.contains(DESCRIBE_QUERY_COMMAND) || operators.contains(CONSTRUCT_QUERY_COMMAND)) {
        invalid = false;
      }
    } else if (parsedQuery instanceof ParsedTupleQuery) {
      if (operators.contains(SELECT_QUERY_COMMAND)) {
        invalid = false;
      }
    }

    if (invalid) {
      throw invalidConfigurationException("Query should be a {} query", queryOperators[0]);
    }
  }
}
