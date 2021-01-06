package org.dotwebstack.framework.backend.rdf4j.scalars;

import com.google.common.collect.ImmutableList;
import graphql.schema.GraphQLScalarType;

public final class Rdf4jScalars {

  public static final GraphQLScalarType IRI = GraphQLScalarType.newScalar()
      .name("IRI")
      .description("IRI type")
      .coercing(new IriCoercing())
      .build();

  public static final GraphQLScalarType MODEL = GraphQLScalarType.newScalar()
      .name("Model")
      .description("Model type")
      .coercing(new ModelCoercing())
      .build();

  public static final GraphQLScalarType SPARQL_QUERY_RESULT = GraphQLScalarType.newScalar()
      .name("SparqlQueryResult")
      .description("Sparql Query Result type")
      .coercing(new SparqlQueryResultCoercing())
      .build();

  public static final ImmutableList<GraphQLScalarType> SCALARS = ImmutableList.of(MODEL, IRI, SPARQL_QUERY_RESULT);

  private Rdf4jScalars() {}

}
