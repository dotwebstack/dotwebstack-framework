package org.dotwebstack.framework.frontend.http.provider.graph;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.eclipse.rdf4j.rio.RDFFormat;

@Provider
@Produces("text/turtle")
public class TurtleGraphProvider extends GraphProviderBase {

  @SuppressWarnings("WeakerAccess")
  public TurtleGraphProvider() {
    super(RDFFormat.TURTLE);
  }

}
