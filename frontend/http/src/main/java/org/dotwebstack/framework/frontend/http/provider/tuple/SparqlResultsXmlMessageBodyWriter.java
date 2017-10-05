package org.dotwebstack.framework.frontend.http.provider.tuple;

import java.io.OutputStream;
import javax.ws.rs.Produces;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.MediaTypes;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.springframework.stereotype.Service;

@Service
@SparqlProvider(resultType = ResultType.TUPLE)
@Produces(MediaTypes.SPARQL_RESULTS_XML)
public final class SparqlResultsXmlMessageBodyWriter
    extends AbstractSparqlResultsMessageBodyWriter {

  SparqlResultsXmlMessageBodyWriter() {
    super(MediaTypes.SPARQL_RESULTS_XML_TYPE);
  }

  @Override
  protected TupleQueryResultWriter createWriter(OutputStream outputStream) {
    return new SPARQLResultsXMLWriter(outputStream);
  }
}
