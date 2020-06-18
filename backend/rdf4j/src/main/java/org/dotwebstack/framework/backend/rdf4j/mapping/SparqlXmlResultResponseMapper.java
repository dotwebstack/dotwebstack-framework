package org.dotwebstack.framework.backend.rdf4j.mapping;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlQueryResult;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
public class SparqlXmlResultResponseMapper implements ResponseMapper {

  static final MimeType SPARQL_RESULT_XML_MEDIA_TYPE = MimeType.valueOf("application/sparql-results+xml");

  @Override
  public boolean supportsOutputMimeType(MimeType mimeType) {
    return SPARQL_RESULT_XML_MEDIA_TYPE.equals(mimeType);
  }

  @Override
  public boolean supportsInputObjectClass(Class<?> clazz) {
    return SparqlQueryResult.class.isAssignableFrom(clazz);
  }

  @Override
  public String toResponse(Object input) {
    if (!(input instanceof SparqlQueryResult)) {
      throw new IllegalArgumentException("Input can only be of the type SparqlQueryResult.");
    }

    SparqlQueryResult sparqlQueryResult = (SparqlQueryResult) input;

    if (sparqlQueryResult.hasResult()) {
      return toXmlResponse(sparqlQueryResult);
    }

    return null;
  }

  private String toXmlResponse(SparqlQueryResult sparqlQueryResult) {
    try (InputStream inputStream = sparqlQueryResult.getInputStream()) {
      return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new ResponseMapperException("Serialization failed.", exception);
    }
  }
}
