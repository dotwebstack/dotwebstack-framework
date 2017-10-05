package org.dotwebstack.framework.frontend.http.provider.tuple;

import java.io.OutputStream;
import javax.ws.rs.Produces;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.MediaTypes;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.springframework.stereotype.Service;

@Service
@SparqlProvider(resultType = ResultType.TUPLE)
@Produces(MediaTypes.SPARQL_RESULTS_JSON)
public final class SparqlResultsJsonMessageBodyWriter
    extends AbstractSparqlResultsMessageBodyWriter {

  SparqlResultsJsonMessageBodyWriter() {
    super(MediaTypes.SPARQL_RESULTS_JSON_TYPE);
  }

  @Override
  protected TupleQueryResultWriter createWriter(OutputStream outputStream) {
    return new SPARQLResultsJSONWriter(outputStream);
  }
}
