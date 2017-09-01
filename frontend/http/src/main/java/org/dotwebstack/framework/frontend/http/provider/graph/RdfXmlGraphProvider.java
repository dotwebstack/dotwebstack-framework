package org.dotwebstack.framework.frontend.http.provider.graph;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.eclipse.rdf4j.rio.RDFFormat;

@Provider
@Produces("application/rdf+xml")
public class RdfXmlGraphProvider extends GraphProviderBase {

  @SuppressWarnings("WeakerAccess")
  public RdfXmlGraphProvider() {
    super(RDFFormat.RDFXML);
  }

}
