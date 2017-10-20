package org.dotwebstack.framework.backend.sparql;

import lombok.NonNull;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparqlBackendInformationProductFactory {

  private QueryEvaluator queryEvaluator;

  @Autowired
  public SparqlBackendInformationProductFactory(@NonNull QueryEvaluator queryEvaluator) {
    this.queryEvaluator = queryEvaluator;
  }

  public InformationProduct create(IRI identifier, String label, Backend backend, Filter filter,
      Model statements) {
    String query = Models.objectString(statements.filter(identifier, ELMO.QUERY, null)).orElseThrow(
        () -> new ConfigurationException(
            String.format("No <%s> statement has been found for a SPARQL information product <%s>.",
                ELMO.QUERY, identifier)));

    ResultType resultType = getQueryType(query);

    return new SparqlBackendInformationProduct.Builder(identifier, (SparqlBackend) backend, query,
        resultType, queryEvaluator, filter).label(label).build();
  }

  private ResultType getQueryType(String query) {
    try {
      ParsedQuery parsedQuery = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, query, null);
      if (parsedQuery instanceof ParsedTupleQuery) {
        return ResultType.TUPLE;
      }
      if (parsedQuery instanceof ParsedGraphQuery) {
        return ResultType.GRAPH;
      }
      throw new ConfigurationException(String.format(
          "Type of query <%s> could not be determined. Only SELECT and CONSTRUCT are supported.",
          query));

    } catch (MalformedQueryException exception) {
      throw new ConfigurationException(String.format(
          "Type of query <%s> could not be determined. "
              + "Query is a malformed query and cannot be processed: %s",
          query, exception.getMessage()));
    }
  }

}
