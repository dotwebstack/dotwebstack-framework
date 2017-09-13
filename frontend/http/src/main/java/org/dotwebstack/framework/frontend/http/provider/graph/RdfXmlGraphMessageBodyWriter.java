package org.dotwebstack.framework.frontend.http.provider.graph;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.eclipse.rdf4j.rio.RDFFormat;

@Provider
@SparqlProvider(resultType = ResultType.GRAPH)
@Produces("application/rdf+xml")
public class RdfXmlGraphMessageBodyWriter extends GraphMessageBodyWriter {

  @SuppressWarnings("WeakerAccess")
  public RdfXmlGraphMessageBodyWriter() {
    super(RDFFormat.RDFXML);
  }

}
