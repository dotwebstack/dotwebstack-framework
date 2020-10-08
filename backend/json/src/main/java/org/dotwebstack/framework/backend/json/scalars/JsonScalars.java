package org.dotwebstack.framework.backend.json.scalars;

import graphql.schema.GraphQLScalarType;

public final class JsonScalars {

  private JsonScalars() {}

  public static final GraphQLScalarType OBJECT = GraphQLScalarType.newScalar()
      .name("JsonObject")
      .description("Object type")
      .coercing(new JsonObjectCoercing())
      .build();
}
