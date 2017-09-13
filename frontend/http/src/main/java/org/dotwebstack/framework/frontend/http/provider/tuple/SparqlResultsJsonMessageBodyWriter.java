package org.dotwebstack.framework.frontend.http.provider.tuple;

import java.io.OutputStream;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;

@Provider
@SparqlProvider(resultType = ResultType.TUPLE)
@Produces(SparqlResultsJsonMessageBodyWriter.MEDIA_TYPE)
public final class SparqlResultsJsonMessageBodyWriter extends TupleMessageBodyWriter {

  public static final String MEDIA_TYPE = "application/sparql-results+json";

  public SparqlResultsJsonMessageBodyWriter() {
    super(MediaType.valueOf(MEDIA_TYPE));
  }

  @Override
  protected TupleQueryResultWriter createWriter(OutputStream outputStream) {
    return new SPARQLResultsJSONWriter(outputStream);
  }
}
