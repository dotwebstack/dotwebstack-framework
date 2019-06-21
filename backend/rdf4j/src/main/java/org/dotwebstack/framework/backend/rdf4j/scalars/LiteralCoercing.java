package org.dotwebstack.framework.backend.rdf4j.scalars;

import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import lombok.NonNull;
import org.dotwebstack.framework.core.scalars.CoreCoercing;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.springframework.stereotype.Component;

@Component
public class LiteralCoercing implements CoreCoercing<Object> {

  @Override
  public boolean isCompatible(@NonNull String className) {
    return className.contains("Literal");
  }

  @Override
  public Object serialize(Object value) throws CoercingSerializeException {
    if (!(value instanceof Literal)) {
      return value;
    }

    Literal literal = (Literal) value;
    IRI dataType = literal.getDatatype();

    if (XMLSchema.BOOLEAN.equals(dataType)) {
      return literal.booleanValue();
    } else if (XMLSchema.INT.equals(dataType)) {
      return literal.intValue();
    } else if (XMLSchema.INTEGER.equals(dataType)) {
      return literal.integerValue();
    } else if (XMLSchema.SHORT.equals(dataType)) {
      return literal.shortValue();
    } else if (XMLSchema.LONG.equals(dataType)) {
      return literal.longValue();
    } else if (XMLSchema.FLOAT.equals(dataType)) {
      return literal.floatValue();
    } else if (XMLSchema.DOUBLE.equals(dataType)) {
      return literal.doubleValue();
    } else if (XMLSchema.DECIMAL.equals(dataType)) {
      return literal.decimalValue();
    } else if (XMLSchema.BYTE.equals(dataType)) {
      return literal.byteValue();
    }

    return literal.stringValue();
  }

  @Override
  public Literal parseValue(Object input) throws CoercingParseValueException {
    return null;
  }

  @Override
  public Literal parseLiteral(Object input) throws CoercingParseLiteralException {
    return null;
  }
}
