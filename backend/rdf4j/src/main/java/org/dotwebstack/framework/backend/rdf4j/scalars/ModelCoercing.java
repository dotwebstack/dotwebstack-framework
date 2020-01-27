package org.dotwebstack.framework.backend.rdf4j.scalars;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseValueException;
import org.eclipse.rdf4j.model.Model;

public class ModelCoercing implements Coercing<Model, String> {
  @Override
  public String serialize(Object model) {
    if (model instanceof Model) {
      return model.toString();
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public Model parseValue(Object model) {
    return parseModel(model);
  }

  @Override
  public Model parseLiteral(Object model) {
    return parseModel(model);
  }

  private Model parseModel(Object model) {
    if (model instanceof Model) {
      return (Model) model;
    }
    throw new CoercingParseValueException(String.format("Unable to parse Model from '%s' type.", model.getClass()
        .getName()));
  }
}
