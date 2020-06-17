package org.dotwebstack.framework.backend.rdf4j.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlQueryResult;

public class SparqlQueryResultCoercing implements Coercing<SparqlQueryResult, SparqlQueryResult> {
  @Override
  public SparqlQueryResult serialize(Object sparqlQueryResult) {
    if (sparqlQueryResult instanceof SparqlQueryResult) {
      return (SparqlQueryResult) sparqlQueryResult;
    }
    throw new IllegalArgumentException("Only supports SparqlQueryResult");
  }

  @Override
  public SparqlQueryResult parseValue(Object sparqlQueryResult) {
    return parseSparqlQueryResult(sparqlQueryResult);
  }

  @Override
  public SparqlQueryResult parseLiteral(Object sparqlQueryResult) {
    return parseSparqlQueryResult(sparqlQueryResult);
  }

  private SparqlQueryResult parseSparqlQueryResult(Object sparqlQueryResult) {
    if (sparqlQueryResult instanceof SparqlQueryResult) {
      return (SparqlQueryResult) sparqlQueryResult;
    }
    throw new CoercingParseValueException(
        String.format("Unable to parse SparqlQueryResult from '%s' type.", sparqlQueryResult.getClass()
            .getName()));
  }
}
