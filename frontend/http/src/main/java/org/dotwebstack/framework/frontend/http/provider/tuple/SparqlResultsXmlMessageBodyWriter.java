package org.dotwebstack.framework.frontend.http.provider.tuple;

import java.io.OutputStream;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;

@Provider
@Produces(SparqlResultsXmlMessageBodyWriter.MEDIA_TYPE)
public final class SparqlResultsXmlMessageBodyWriter extends TupleMessageBodyWriter {

  public static final String MEDIA_TYPE = "application/sparql-results+xml";

  public SparqlResultsXmlMessageBodyWriter() {
    super(MediaType.valueOf(MEDIA_TYPE));
  }

  @Override
  protected TupleQueryResultWriter createWriter(OutputStream outputStream) {
    return new SPARQLResultsXMLWriter(outputStream);
  }
}
