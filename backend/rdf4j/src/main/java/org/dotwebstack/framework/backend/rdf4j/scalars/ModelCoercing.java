package org.dotwebstack.framework.backend.rdf4j.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.eclipse.rdf4j.model.Model;

public class ModelCoercing implements Coercing<String, String> {
  @Override
  public String serialize(Object model) throws CoercingSerializeException {
    if (model instanceof Model) {
      return model.toString();
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public String parseValue(Object model) throws CoercingParseValueException {
    if (model instanceof Model) {
      return model.toString();
    }
    throw new CoercingParseValueException(String.format("Unable to parse Model from '%s' type.", model.getClass()
        .getName()));
  }

  @Override
  public String parseLiteral(Object model) throws CoercingParseLiteralException {
    if (model instanceof Model) {
      return model.toString();
    }
    throw new CoercingParseValueException(String.format("Unable to parse Model from '%s' type.", model.getClass()
        .getName()));
  }
}
