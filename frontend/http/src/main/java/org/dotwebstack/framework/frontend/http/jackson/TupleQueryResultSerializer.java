package org.dotwebstack.framework.frontend.http.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.dotwebstack.framework.LiteralDataTypes;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
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

    if (LiteralDataTypes.BOOLEAN_DATA_TYPES.contains(literalValue.getDatatype())) {
      return literalValue.booleanValue();
    }

    if (LiteralDataTypes.INTEGER_DATA_TYPES.contains(literalValue.getDatatype())) {
      return literalValue.integerValue();
    }

    if (LiteralDataTypes.DECIMAL_DATA_TYPES.contains(literalValue.getDatatype())) {
      return literalValue.decimalValue();
    }

    return literalValue.stringValue();
  }

}
