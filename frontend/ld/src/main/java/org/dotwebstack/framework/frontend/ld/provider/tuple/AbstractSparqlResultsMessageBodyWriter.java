package org.dotwebstack.framework.frontend.ld.provider.tuple;

import java.io.OutputStream;
import javax.ws.rs.core.MediaType;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;

public abstract class AbstractSparqlResultsMessageBodyWriter
    extends AbstractTupleMessageBodyWriter {

  AbstractSparqlResultsMessageBodyWriter(MediaType mediaType) {
    super(mediaType);
  }

  protected abstract TupleQueryResultWriter createWriter(OutputStream outputStream);


  @Override
  public void write(TupleQueryResult tupleQueryResult, OutputStream outputStream) {
    TupleQueryResultWriter writer = createWriter(outputStream);
    QueryResults.report(tupleQueryResult, writer);
  }

}
