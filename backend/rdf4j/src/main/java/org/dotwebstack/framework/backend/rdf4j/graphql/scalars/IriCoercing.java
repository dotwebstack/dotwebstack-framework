package org.dotwebstack.framework.backend.rdf4j.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

class IriCoercing implements Coercing<IRI, IRI> {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  @Override
  public IRI serialize(@NonNull Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IRI parseValue(@NonNull Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IRI parseLiteral(@NonNull Object o) {
    if (!(o instanceof StringValue)) {
      throw new CoercingParseLiteralException(
          String.format("Unable to parse IRI from '%s' type.", o.getClass().getName()));
    }

    String value = ((StringValue) o).getValue();

    try {
      return VF.createIRI(value);
    } catch (IllegalArgumentException e) {
      throw new CoercingParseLiteralException(String
          .format("Unable to parse IRI from string value '%s'.", value), e);
    }
  }

}
