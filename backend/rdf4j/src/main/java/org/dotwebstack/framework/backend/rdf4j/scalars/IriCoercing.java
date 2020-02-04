package org.dotwebstack.framework.backend.rdf4j.scalars;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.createIri;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import java.net.URI;
import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;

public class IriCoercing implements Coercing<IRI, IRI> {

  @Override
  public IRI serialize(@NonNull Object value) {
    if (value instanceof IRI) {
      return (IRI) value;
    }

    if (value instanceof String) {
      return createIri((String) value);
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public IRI parseValue(@NonNull Object value) {
    if (value instanceof URI || value instanceof String) {
      return createIri(value.toString());
    }
    throw new CoercingParseValueException(String.format("Unable to parse IRI from '%s' type.", value.getClass()
        .getName()));
  }

  @Override
  public IRI parseLiteral(@NonNull Object value) {
    if (value instanceof IRI) {
      return (IRI) value;
    }

    if (!(value instanceof StringValue)) {
      throw new CoercingParseLiteralException(String.format("Unable to parse IRI from '%s' type.", value.getClass()
          .getName()));
    }

    String valueStr = ((StringValue) value).getValue();

    try {
      return createIri(valueStr);
    } catch (IllegalArgumentException e) {
      throw new CoercingParseLiteralException(String.format("Unable to parse IRI from string value '%s'.", valueStr),
          e);
    }
  }
}
