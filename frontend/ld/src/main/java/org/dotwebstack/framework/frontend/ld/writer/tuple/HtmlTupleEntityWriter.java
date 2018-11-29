package org.dotwebstack.framework.frontend.ld.writer.tuple;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.HtmlTupleEntity;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.springframework.stereotype.Service;

@Service
@EntityWriter(resultType = ResultType.GRAPH)
@Produces({"text/html"})
public class HtmlTupleEntityWriter implements MessageBodyWriter<HtmlTupleEntity> {

  private final List<MediaType> mediaTypes = Collections.singletonList(MediaTypes.TEXT_HTML_TYPE);

  private String htmlString;

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
                             MediaType mediaType) {
    return HtmlTupleEntity.class.isAssignableFrom(type) && mediaTypes.contains(mediaType);
  }

  @Override
  public long getSize(HtmlTupleEntity htmlTupleEntity, Class<?> type, Type genericType,
                      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(HtmlTupleEntity htmlTupleEntity, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {

  }
}

