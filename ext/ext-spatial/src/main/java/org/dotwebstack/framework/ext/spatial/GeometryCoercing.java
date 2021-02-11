package org.dotwebstack.framework.ext.spatial;

import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;

public class GeometryCoercing implements Coercing<Object, Object> {

  @Override
  public Object serialize(Object value) {
    if (value instanceof FormattedGeometry) {
      return ((FormattedGeometry) value).get();
    }

    throw new CoercingSerializeException("Invalid type.");
  }

  @Override
  public Object parseValue(Object value) {
    throw new UnsupportedOperationException("Method is not implemented.");
  }

  @Override
  public Object parseLiteral(Object value) {
    throw new UnsupportedOperationException("Method is not implemented.");
  }
}
