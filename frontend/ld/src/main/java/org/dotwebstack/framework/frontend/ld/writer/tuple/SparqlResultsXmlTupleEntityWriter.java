package org.dotwebstack.framework.frontend.ld.writer.tuple;

import java.io.OutputStream;
import javax.ws.rs.Produces;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.springframework.stereotype.Service;

@Service
@EntityWriter(resultType = ResultType.TUPLE)
@Produces(MediaTypes.SPARQL_RESULTS_XML)
public final class SparqlResultsXmlTupleEntityWriter
    extends AbstractSparqlResultsTupleEntityWriter {

  SparqlResultsXmlTupleEntityWriter() {
    super(MediaTypes.SPARQL_RESULTS_XML_TYPE);
  }

  @Override
  protected TupleQueryResultWriter createWriter(OutputStream outputStream) {
    return new SPARQLResultsXMLWriter(outputStream);
  }
}
