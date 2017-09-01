package org.dotwebstack.framework.frontend.http.provider.graph;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.eclipse.rdf4j.rio.RDFFormat;

@Provider
@Produces("application/trig")
public class TriGGraphProvider extends GraphProviderBase {

  TriGGraphProvider() {
    super(RDFFormat.TRIG);
  }

}
