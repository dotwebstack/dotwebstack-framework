package org.dotwebstack.framework.core.scalars;

import graphql.schema.Coercing;

public class ObjectCoercing implements Coercing<Object, Object> {
  @Override
  public Object serialize(Object object) {
    return object;
  }

  @Override
  public Object parseValue(Object object) {
    return object;
  }

  @Override
  public Object parseLiteral(Object object) {
    return object;
  }
}
