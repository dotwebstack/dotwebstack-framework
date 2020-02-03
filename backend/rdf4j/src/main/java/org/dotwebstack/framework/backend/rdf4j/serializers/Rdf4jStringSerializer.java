package org.dotwebstack.framework.backend.rdf4j.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.eclipse.rdf4j.model.IRI;

public class Rdf4jStringSerializer extends JsonSerializer<IRI> {

  @Override
  public void serialize(IRI iri, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(iri.stringValue());
  }

  @Override
  public Class<IRI> handledType() {
    return IRI.class;
  }
}
