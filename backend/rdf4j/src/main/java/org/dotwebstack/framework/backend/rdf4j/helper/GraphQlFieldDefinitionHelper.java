package org.dotwebstack.framework.backend.rdf4j.helper;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.eclipse.rdf4j.query.QueryLanguage.SPARQL;

import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import java.util.Arrays;
import java.util.List;
import javax.validation.constraints.NotEmpty;
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
      @NonNull GraphQLScalarType scalarType) {
    return ((GraphQLNamedType) type).getName()
        .equals(scalarType.getName());
  }

  public static void validateQueryHasCommand(@NonNull String staticSparqlQuery, @NotEmpty String... queryOperators) {
    if (isInvalid(staticSparqlQuery, queryOperators)) {
      throw invalidConfigurationException("Query should be a {} query", queryOperators[0]);
    }
  }

  private static boolean isInvalid(String staticSparqlQuery, String... queryOperators) {
    ParsedQuery parsedQuery = QueryParserUtil.parseQuery(SPARQL, staticSparqlQuery, null);
    List<String> operators = Arrays.asList(queryOperators);

    if (parsedQuery instanceof ParsedGraphQuery) {
      if (operators.contains(DESCRIBE_QUERY_COMMAND) || operators.contains(CONSTRUCT_QUERY_COMMAND)) {
        return false;
      }
    } else if (parsedQuery instanceof ParsedTupleQuery && operators.contains(SELECT_QUERY_COMMAND)) {
      return false;
    }
    return true;
  }
}
