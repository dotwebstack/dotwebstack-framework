package org.dotwebstack.framework.frontend.http.provider.tuple;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.http.jackson.TupleQueryResultSerializer;
import org.eclipse.rdf4j.query.TupleQueryResult;

public abstract class AbstractJsonGeneratorMessageBodyWriter
    extends AbstractTupleMessageBodyWriter {

  AbstractJsonGeneratorMessageBodyWriter(MediaType mediaType) {
    super(mediaType);
  }

  protected abstract JsonFactory createFactory();

  protected abstract TupleQueryResultSerializer createSerializer();

  @Override
  public void write(TupleQueryResult tupleQueryResult, OutputStream outputStream)
      throws IOException {
    JsonGenerator jsonGenerator = createFactory().createGenerator(outputStream);

    createSerializer().serialize(tupleQueryResult, jsonGenerator, null);

    jsonGenerator.close();
  }
}
