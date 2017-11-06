package org.dotwebstack.framework.frontend.ld.writer.tuple;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.eclipse.rdf4j.query.TupleQueryResult;

public abstract class AbstractTupleEntityWriter implements MessageBodyWriter<TupleEntity> {

  private MediaType mediaType;

  protected AbstractTupleEntityWriter(@NonNull MediaType mediaType) {
    this.mediaType = mediaType;
  }

  protected abstract void write(TupleQueryResult tupleQueryResult, OutputStream outputStream)
      throws IOException;

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return TupleEntity.class.isAssignableFrom(type) && this.mediaType.equals(mediaType);
  }

  @Override
  public long getSize(TupleEntity tupleEntity, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
    return -1;
  }

  @Override
  public void writeTo(TupleEntity tupleEntity, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
      OutputStream outputStream) throws IOException {
    write(tupleEntity.getQueryResult(), outputStream);
  }

}
