package org.dotwebstack.framework.backend.rdf4j.mapping;

import com.google.common.base.Charsets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlQueryResult;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLParser;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
public class SparqlJsonResultResponseMapper implements ResponseMapper {

  private MimeType mimeType = MimeType.valueOf("application/sparql-results+json");

  @Override
  public boolean supportsOutputMimeType(MimeType mimeType) {
    return this.mimeType.equals(mimeType);
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

    SPARQLResultsXMLParser parser = new SPARQLResultsXMLParser();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    SPARQLResultsJSONWriter writer = new SPARQLResultsJSONWriter(outputStream);
    parser.setQueryResultHandler(writer);

    SparqlQueryResult sparqlQueryResult = (SparqlQueryResult) input;

    try (InputStream inputStream = sparqlQueryResult.getInputStream()) {
      parser.parseQueryResult(inputStream);
    } catch (IOException exception) {
      throw new ResponseMapperException("Serialization failed.", exception);
    }
    return outputStream.toString(Charsets.UTF_8);
  }


}
