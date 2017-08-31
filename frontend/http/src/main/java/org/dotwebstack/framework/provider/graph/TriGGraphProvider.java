package org.dotwebstack.framework.provider.graph;

import org.eclipse.rdf4j.rio.RDFFormat;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("application/trig")
public class TriGGraphProvider extends GraphProviderBase {

    public TriGGraphProvider() {
        super(RDFFormat.TRIG);
    }

}
