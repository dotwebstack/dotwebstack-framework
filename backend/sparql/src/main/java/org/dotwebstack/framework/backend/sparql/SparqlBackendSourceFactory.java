package org.dotwebstack.framework.backend.sparql;

import java.util.Objects;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.backend.BackendSourceFactory;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedDescribeQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendSourceFactory implements BackendSourceFactory {

  private QueryEvaluator queryEvaluator;

  @Autowired
  public SparqlBackendSourceFactory(QueryEvaluator queryEvaluator) {
    this.queryEvaluator = Objects.requireNonNull(queryEvaluator);
  }

  public BackendSource create(Backend backend, Model statements) {
    String query = Models.objectString(statements.filter(null, ELMO.QUERY, null)).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for backend source <%s>.", ELMO.QUERY,
                backend.getIdentifier())));

    SparqlQueryType queryType = getQueryType(query);

    return new SparqlBackendSource.Builder((SparqlBackend) backend, query, queryType,
        queryEvaluator).build();
  }

  private SparqlQueryType getQueryType(String query) {
    try {
      ParsedQuery parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null);
      if (parsedQuery instanceof ParsedDescribeQuery) {
        return SparqlQueryType.DESCRIBE;
      } else if (parsedQuery instanceof ParsedTupleQuery) {
        return SparqlQueryType.TUPLE;
      } else if (parsedQuery instanceof ParsedGraphQuery) {
        return SparqlQueryType.GRAPH;
      } else if (parsedQuery instanceof ParsedBooleanQuery) {
        return SparqlQueryType.BOOLEAN;
      }

      throw new ConfigurationException(
          String.format("Type of query <%s> could not be determined.", query));

    } catch (MalformedQueryException exception) {
      throw new ConfigurationException(String.format(
          "Type of query <%s> could not be determined. "
              + "Query is a malformed query and cannot be processed: %s",
          query, exception.getMessage()));
    }
  }

}
