package org.dotwebstack.framework.backend.rdf4j.graphql.scalars;

import graphql.schema.GraphQLScalarType;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Rdf4jScalars {

  public static final GraphQLScalarType IRI = GraphQLScalarType
      .newScalar()
      .name("IRI")
      .description("IRI type")
      .coercing(new IriCoercing())
      .build();

}
