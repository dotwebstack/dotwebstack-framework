package org.dotwebstack.framework.frontend.ld.writer.tuple;

import java.io.OutputStream;
import javax.ws.rs.Produces;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.springframework.stereotype.Service;

@Service
@EntityWriter(resultType = ResultType.TUPLE)
@Produces(MediaTypes.CSV)
public final class SparqlResultsCsvTupleEntityWriter
    extends AbstractSparqlResultsTupleEntityWriter {

  SparqlResultsCsvTupleEntityWriter() {
    super(MediaTypes.CSV_TYPE);
  }

  @Override
  protected TupleQueryResultWriter createWriter(OutputStream outputStream) {
    return new SPARQLResultsCSVWriter(outputStream);
  }
}
