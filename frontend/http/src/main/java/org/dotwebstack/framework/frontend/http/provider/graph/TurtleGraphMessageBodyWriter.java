package org.dotwebstack.framework.frontend.http.provider.graph;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.eclipse.rdf4j.rio.RDFFormat;

@Provider
@Produces("text/turtle")
public class TurtleGraphMessageBodyWriter extends GraphMessageBodyWriter {

  @SuppressWarnings("WeakerAccess")
  public TurtleGraphMessageBodyWriter() {
    super(RDFFormat.TURTLE);
  }

}
