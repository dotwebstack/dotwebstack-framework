package org.dotwebstack.framework.frontend.http.error;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Writer for generating html error pages
 *
 * <p>The writing looks into the /errorpages subfolder of the resourcePath folder,
 * The resourcePath variable is as defined in application.yml
 * For any error, a Freemarker template file is expected, with an optional locale
 * For example:
 * 404_nl.html will be used for a 404 http error, for nl language users
 * 404.html will be used for any 404 http error for which a locale template doesn't exists
 * When a specific error page template cannot be found, the writer falls back to a
 * simple html template, stating title, status code and error details
 * </p>
 *
 */
@Service
@Produces({MediaType.TEXT_HTML})
public class HtmlProblemDetailsWriter implements MessageBodyWriter<ProblemDetails> {

  private HtmlTemplateProcessor htmlTemplateProcessor;

  @Autowired
  HtmlProblemDetailsWriter(@NonNull HtmlTemplateProcessor htmlTemplateProcessor) {
    this.htmlTemplateProcessor = htmlTemplateProcessor;
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return ProblemDetails.class.isAssignableFrom(type)
        && MediaType.TEXT_HTML_TYPE.equals(mediaType);
  }

  @Override
  public long getSize(ProblemDetails problemDetails, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
    return -1;
  }

  @Override
  public void writeTo(ProblemDetails problemDetails, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
      OutputStream outputStream) throws IOException {
    htmlTemplateProcessor.process(problemDetails, outputStream);
  }

}
