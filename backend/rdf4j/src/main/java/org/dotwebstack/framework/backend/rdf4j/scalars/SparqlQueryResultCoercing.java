package org.dotwebstack.framework.backend.rdf4j.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlQueryResult;

public class SparqlQueryResultCoercing implements Coercing<SparqlQueryResult, SparqlQueryResult> {
  @Override
  public SparqlQueryResult serialize(Object model) {
    if (model instanceof SparqlQueryResult) {
      return (SparqlQueryResult) model;
    }
    throw new IllegalArgumentException("Only supports RDF4j Model");
  }

  @Override
  public SparqlQueryResult parseValue(Object model) {
    return parseModel(model);
  }

  @Override
  public SparqlQueryResult parseLiteral(Object model) {
    return parseModel(model);
  }

  private SparqlQueryResult parseModel(Object model) {
    if (model instanceof SparqlQueryResult) {
      return (SparqlQueryResult) model;
    }
    throw new CoercingParseValueException(
        String.format("Unable to parse SparqlQueryResult from '%s' type.", model.getClass()
            .getName()));
  }
}
