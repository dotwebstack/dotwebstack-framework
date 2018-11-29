package org.dotwebstack.framework.frontend.ld.writer.graph;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.ws.http.HTTPException;

import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.HtmlGraphEntity;

import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.stereotype.Service;

@Service
@EntityWriter(resultType = ResultType.GRAPH)
@Produces({"text/html"})
public class HtmlGraphEntityWriter implements MessageBodyWriter<HtmlGraphEntity> {

  private final List<MediaType> mediaTypes = Collections.singletonList(MediaTypes.TEXT_HTML_TYPE);

  private String htmlString;

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
                             MediaType mediaType) {
    return HtmlGraphEntity.class.isAssignableFrom(type) && mediaTypes.contains(mediaType);
  }

  @Override
  public long getSize(HtmlGraphEntity htmlGraphEntity, Class<?> type, Type genericType,
                      Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(HtmlGraphEntity s, Class<?> representation, Type type,
                      Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream)
      throws IOException, WebApplicationException {
    Model model = QueryResults.asModel(s.getQueryResult());
    Rio.write(model, outputStream, RDFFormat.JSONLD);
  }
}

