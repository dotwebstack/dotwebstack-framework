package org.dotwebstack.framework.frontend.ld.provider.graph;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.ld.provider.MediaTypes;
import org.dotwebstack.framework.frontend.ld.provider.SparqlProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Service;

@Service
@SparqlProvider(resultType = ResultType.GRAPH)
@Produces({MediaTypes.LDJSON, MediaType.APPLICATION_JSON})
public class JsonLdGraphMessageBodyWriter extends GraphMessageBodyWriter {

  JsonLdGraphMessageBodyWriter() {
    super(RDFFormat.JSONLD, MediaTypes.LDJSON_TYPE, MediaType.APPLICATION_JSON_TYPE);
  }

}
