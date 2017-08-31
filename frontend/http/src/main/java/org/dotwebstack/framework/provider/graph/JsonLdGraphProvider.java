package org.dotwebstack.framework.provider.graph;

import org.eclipse.rdf4j.rio.RDFFormat;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("application/ld+json")
public class JsonLdGraphProvider extends GraphProviderBase {

    public JsonLdGraphProvider() {
        super(RDFFormat.JSONLD);
    }

}
