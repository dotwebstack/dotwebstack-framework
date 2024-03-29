package org.dotwebstack.framework.core.scalars;

import graphql.schema.GraphQLScalarType;

public final class CoreScalars {

  public static final GraphQLScalarType DATE = GraphQLScalarType.newScalar()
      .name("Date")
      .description("Date type")
      .coercing(new DateCoercing())
      .build();

  public static final GraphQLScalarType DATETIME = GraphQLScalarType.newScalar()
      .name("DateTime")
      .description("DateTime type")
      .coercing(new DateTimeCoercing())
      .build();

  private CoreScalars() {}
}
