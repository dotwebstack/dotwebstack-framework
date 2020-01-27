package org.dotwebstack.framework.backend.rdf4j.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import org.eclipse.rdf4j.model.Model;

public class ModelCoercing implements Coercing<String, String> {
  @Override
  public String serialize(Object model) {
    if (model instanceof Model) {
      return model.toString();
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public String parseValue(Object model) {
    return parseModel(model);
  }

  @Override
  public String parseLiteral(Object model) {
    return parseModel(model);
  }

  private String parseModel(Object model) {
    if (model instanceof Model) {
      return model.toString();
    }
    throw new CoercingParseValueException(String.format("Unable to parse Model from '%s' type.", model.getClass()
        .getName()));
  }
}
