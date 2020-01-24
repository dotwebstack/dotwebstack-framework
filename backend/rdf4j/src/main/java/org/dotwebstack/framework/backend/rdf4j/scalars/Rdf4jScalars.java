package org.dotwebstack.framework.backend.rdf4j.scalars;

import graphql.schema.GraphQLScalarType;
import java.util.ArrayList;
import java.util.List;

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

  public static final List<GraphQLScalarType> SCALARS = new ArrayList<>() {
    {
      add(IRI);
      add(MODEL);
    }
  };

  private Rdf4jScalars() {}

}
