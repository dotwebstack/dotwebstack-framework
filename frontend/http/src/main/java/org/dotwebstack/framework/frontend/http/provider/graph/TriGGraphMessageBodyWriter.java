package org.dotwebstack.framework.frontend.http.provider.graph;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.eclipse.rdf4j.rio.RDFFormat;

@Provider
@SparqlProvider(resultType = ResultType.GRAPH)
@Produces("application/trig")
public class TriGGraphMessageBodyWriter extends GraphMessageBodyWriter {

  @SuppressWarnings("WeakerAccess")
  public TriGGraphMessageBodyWriter() {
    super(RDFFormat.TRIG);
  }

}
