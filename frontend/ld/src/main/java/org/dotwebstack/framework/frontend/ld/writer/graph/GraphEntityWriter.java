package org.dotwebstack.framework.frontend.ld.writer.graph;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public abstract class GraphEntityWriter implements MessageBodyWriter<GraphEntity> {

  private final RDFFormat format;

  private final List<MediaType> mediaTypes;

  protected GraphEntityWriter(@NonNull RDFFormat format,
      @NonNull MediaType... supportedMediaTypes) {
    this.format = format;
    this.mediaTypes = supportedMediaTypes.length == 0
        ? format.getMIMETypes().stream().map(MediaType::valueOf).collect(toList())
        : Arrays.asList(supportedMediaTypes);
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return GraphEntity.class.isAssignableFrom(type) && mediaTypes.contains(mediaType);
  }

  @Override
  public long getSize(GraphEntity graphEntity, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
    return -1;
  }

  @Override
  public void writeTo(GraphEntity graphEntity, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
      OutputStream outputStream) throws IOException {
    Model model = QueryResults.asModel(graphEntity.getQueryResult());
    Rio.write(model, outputStream, format);
  }

}

