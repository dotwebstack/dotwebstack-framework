package org.dotwebstack.framework.frontend.http.provider.graph;

import org.eclipse.rdf4j.rio.RDFFormat;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("text/turtle")
public class TurtleGraphProvider extends GraphProviderBase {

    public TurtleGraphProvider() {
        super(RDFFormat.TURTLE);
    }

}
