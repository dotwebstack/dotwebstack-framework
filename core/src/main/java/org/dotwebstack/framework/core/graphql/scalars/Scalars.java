package org.dotwebstack.framework.core.graphql.scalars;

import graphql.schema.GraphQLScalarType;

public final class Scalars {

  public static final GraphQLScalarType DATE = GraphQLScalarType
      .newScalar()
      .name("Date")
      .description("Date type")
      .coercing(new DateCoercing())
      .build();

  public static final GraphQLScalarType DATETIME = GraphQLScalarType
      .newScalar()
      .name("DateTime")
      .description("DateTime type")
      .coercing(new DateTimeCoercing())
      .build();

  public static final GraphQLScalarType IRI = GraphQLScalarType
      .newScalar()
      .name("IRI")
      .description("IRI type")
      .coercing(new IriCoercing())
      .build();

  private Scalars() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Scalars.class));
  }

}
