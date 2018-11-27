package org.dotwebstack.framework.frontend.ld.writer.graph;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.ws.http.HTTPException;

import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.ld.entity.HtmlGraphEntity;

import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.springframework.stereotype.Service;

@Service
@EntityWriter(resultType = ResultType.GRAPH)
@Produces({"text/html"})
public class HtmlGraphEntityWriter implements MessageBodyWriter<HtmlGraphEntity> {

  private final List<MediaType> mediaTypes = Collections.singletonList(MediaType.TEXT_HTML_TYPE);

  private String htmlString;

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
                             MediaType mediaType) {
    return HtmlGraphEntityWriter.class.isAssignableFrom(type) && mediaTypes.contains(mediaType);
  }

  @Override
  public long getSize(HtmlGraphEntity htmlGraphEntity, Class<?> type, Type genericType,
                      Annotation[] annotations, MediaType mediaType) {
    return 0;
  }

  @Override
  public void writeTo(HtmlGraphEntity s, Class<?> representation, Type type,
                      Annotation[] annotations, MediaType mediaType,
                      MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream)
      throws IOException, WebApplicationException {
    Template htmlTemplate = s.getRepresentation().getHtmlTemplate();

    if (htmlTemplate != null) {
      Map<String, Object> freeMarkerDataModel = new HashMap<>();
      freeMarkerDataModel.put("result", s.getRepresentation().getIdentifier());
      try {
        StringWriter stringWriter = new StringWriter();
        htmlTemplate.process(freeMarkerDataModel, stringWriter);
        StringBuffer buffer = stringWriter.getBuffer();
        this.htmlString = buffer != null ? buffer.toString() : "UNKNOWN";
      } catch (IOException e) {
        System.out.print("faal");
      } catch (TemplateException e) {
        System.out.print("faal2");
      }
    }
    throw new HTTPException(406);
  }
}

