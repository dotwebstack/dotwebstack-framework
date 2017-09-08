package org.dotwebstack.framework.frontend.http.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.TupleQueryResult;

public class TupleQueryResultSerializer extends JsonSerializer<TupleQueryResult> {

  @Override
  public void serialize(TupleQueryResult tupleQueryResult, JsonGenerator jsonGenerator,
      SerializerProvider serializerProvider) throws IOException {
    jsonGenerator.writeStartArray();

    while (tupleQueryResult.hasNext()) {
      jsonGenerator.writeStartObject();

      tupleQueryResult.next().forEach(binding -> {
        try {
          jsonGenerator.writeObjectField(binding.getName(), serializeValue(binding.getValue()));
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });

      jsonGenerator.writeEndObject();
    }

    jsonGenerator.writeEndArray();
  }

  private Object serializeValue(Value value) {
    if (!(value instanceof Literal)) {
      return value.stringValue();
    }

    Literal literalValue = (Literal) value;

    if (literalValue.getDatatype().equals(XMLSchema.BOOLEAN)) {
      return literalValue.booleanValue();
    }

    if (literalValue.getDatatype().equals(XMLSchema.BYTE)
        || literalValue.getDatatype().equals(XMLSchema.INT)
        || literalValue.getDatatype().equals(XMLSchema.INTEGER)
        || literalValue.getDatatype().equals(XMLSchema.LONG)
        || literalValue.getDatatype().equals(XMLSchema.NEGATIVE_INTEGER)
        || literalValue.getDatatype().equals(XMLSchema.NON_NEGATIVE_INTEGER)
        || literalValue.getDatatype().equals(XMLSchema.NON_POSITIVE_INTEGER)
        || literalValue.getDatatype().equals(XMLSchema.POSITIVE_INTEGER)
        || literalValue.getDatatype().equals(XMLSchema.SHORT)
        || literalValue.getDatatype().equals(XMLSchema.UNSIGNED_LONG)
        || literalValue.getDatatype().equals(XMLSchema.UNSIGNED_INT)
        || literalValue.getDatatype().equals(XMLSchema.UNSIGNED_SHORT)
        || literalValue.getDatatype().equals(XMLSchema.UNSIGNED_BYTE)) {
      return literalValue.integerValue();
    }

    if (literalValue.getDatatype().equals(XMLSchema.DECIMAL)
        || literalValue.getDatatype().equals(XMLSchema.FLOAT)
        || literalValue.getDatatype().equals(XMLSchema.DOUBLE)) {
      return literalValue.decimalValue();
    }

    return literalValue.stringValue();
  }

}
