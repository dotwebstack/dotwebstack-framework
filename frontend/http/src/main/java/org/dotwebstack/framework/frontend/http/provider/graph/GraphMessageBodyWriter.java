package org.dotwebstack.framework.frontend.http.provider.graph;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import lombok.NonNull;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public abstract class GraphMessageBodyWriter implements MessageBodyWriter<GraphQueryResult> {

  private final RDFFormat format;

  protected GraphMessageBodyWriter(@NonNull RDFFormat format) {
    this.format = format;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return GraphQueryResult.class.isAssignableFrom(type)
        && format.getMIMETypes().contains(mediaType.toString());
  }

  @Override
  public long getSize(GraphQueryResult model, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
    return -1;
  }

  @Override
  public void writeTo(GraphQueryResult queryResult, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
      OutputStream outputStream) throws IOException {
    Model model = QueryResults.asModel(queryResult);
    Rio.write(model, outputStream, format);
  }

}

