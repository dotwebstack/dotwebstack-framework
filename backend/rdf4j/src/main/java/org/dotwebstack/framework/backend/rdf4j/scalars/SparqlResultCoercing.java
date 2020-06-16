package org.dotwebstack.framework.backend.rdf4j.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlResult;

public class SparqlResultCoercing implements Coercing<SparqlResult, SparqlResult> {
  @Override
  public SparqlResult serialize(Object model) {
    if (model instanceof SparqlResult) {
      return (SparqlResult) model;
    }
    throw new IllegalArgumentException("Only supports RDF4j Model");
  }

  @Override
  public SparqlResult parseValue(Object model) {
    return parseModel(model);
  }

  @Override
  public SparqlResult parseLiteral(Object model) {
    return parseModel(model);
  }

  private SparqlResult parseModel(Object model) {
    if (model instanceof SparqlResult) {
      return (SparqlResult) model;
    }
    throw new CoercingParseValueException(String.format("Unable to parse SparqlResult from '%s' type.", model.getClass()
        .getName()));
  }
}
