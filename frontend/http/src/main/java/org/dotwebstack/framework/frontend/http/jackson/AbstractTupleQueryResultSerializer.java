package org.dotwebstack.framework.frontend.http.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import lombok.NonNull;
import org.dotwebstack.framework.LiteralDataTypes;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;

public abstract class AbstractTupleQueryResultSerializer extends JsonSerializer<TupleQueryResult>
    implements TupleQueryResultSerializer {

  protected abstract void writeStart(JsonGenerator jsonGenerator) throws IOException;

  protected abstract void writeStartItem(JsonGenerator jsonGenerator) throws IOException;

  protected abstract void writeEnd(JsonGenerator jsonGenerator) throws IOException;

  protected abstract void writeEndItem(JsonGenerator jsonGenerator) throws IOException;

  @Override
  public void serialize(@NonNull TupleQueryResult tupleQueryResult,
      @NonNull JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {

    writeStart(jsonGenerator);

    while (tupleQueryResult.hasNext()) {
      writeStartItem(jsonGenerator);

      BindingSet bindingSet = tupleQueryResult.next();
      for (Binding binding : bindingSet) {
        jsonGenerator.writeObjectField(binding.getName(), serializeValue(binding.getValue()));
      }

      writeEndItem(jsonGenerator);
    }

    writeEnd(jsonGenerator);
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
