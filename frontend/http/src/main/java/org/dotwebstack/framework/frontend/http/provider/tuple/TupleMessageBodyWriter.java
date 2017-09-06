package org.dotwebstack.framework.frontend.http.provider.tuple;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;

public abstract class TupleMessageBodyWriter implements MessageBodyWriter<TupleQueryResult> {

  private MediaType mediaType;

  TupleMessageBodyWriter(MediaType mediaType) {
    this.mediaType = Objects.requireNonNull(mediaType);
  }

  protected abstract TupleQueryResultWriter createWriter(OutputStream outputStream);

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return TupleQueryResult.class.isAssignableFrom(type) && this.mediaType.equals(mediaType);
  }

  @Override
  public long getSize(TupleQueryResult tupleQueryResult, Class<?> aClass, Type type,
      Annotation[] annotations, MediaType mediaType) {
    // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
    return -1;
  }

  @Override
  public void writeTo(TupleQueryResult tupleQueryResult, Class<?> aClass, Type type,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
      OutputStream outputStream) throws IOException {
    TupleQueryResultWriter writer = createWriter(outputStream);
    QueryResults.report(tupleQueryResult, writer);
  }
}
