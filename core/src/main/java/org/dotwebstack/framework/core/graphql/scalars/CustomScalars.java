package org.dotwebstack.framework.core.graphql.scalars;

import graphql.schema.GraphQLScalarType;

final class CustomScalars {

  static final GraphQLScalarType DATE = GraphQLScalarType
      .newScalar()
      .name("date")
      .description("A custom scalar that handles dates")
      .coercing(new DateCoercing())
      .build();

  private CustomScalars() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", CustomScalars.class));
  }

}
