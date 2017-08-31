package org.dotwebstack.framework.provider.graph;

import org.eclipse.rdf4j.rio.RDFFormat;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("application/rdf+xml")
public class RdfXmlGraphProvider extends GraphProviderBase {

    public RdfXmlGraphProvider() {
        super(RDFFormat.RDFXML);
    }

}
