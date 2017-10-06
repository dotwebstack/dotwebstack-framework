package org.dotwebstack.framework.frontend.http.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;

public class TupleQueryResultJsonSerializer extends AbstractTupleQueryResultSerializer {

  @Override
  protected void writeStart(JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeStartArray();
  }

  @Override
  protected void writeStartItem(JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeStartObject();
  }

  @Override
  protected void writeEndItem(JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeEndObject();
  }

  @Override
  protected void writeEnd(JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeEndArray();
  }

}
