package org.dotwebstack.framework.backend.rdf4j.mapping;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlResult;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
public class SparqlXmlResultResponseMapper implements ResponseMapper {

  private MimeType mimeType = MimeType.valueOf("application/sparql-results+xml");

  @Override
  public boolean supportsOutputMimeType(MimeType mimeType) {
    return this.mimeType.equals(mimeType);
  }

  @Override
  public boolean supportsInputObjectClass(Class<?> clazz) {
    return SparqlResult.class.isAssignableFrom(clazz);
  }

  @Override
  public String toResponse(Object input) {
    if (!(input instanceof SparqlResult)) {
      throw new IllegalArgumentException("Input can only be of the type SparqlResult.");
    }

    SparqlResult sparqlResult = (SparqlResult) input;

    try (InputStream inputStream = sparqlResult.getInputStream()) {
      return IOUtils.toString(inputStream, Charsets.UTF_8);
    } catch (IOException exception) {
      throw new RuntimeException("Serialization failed.", exception);
    }
  }
}
