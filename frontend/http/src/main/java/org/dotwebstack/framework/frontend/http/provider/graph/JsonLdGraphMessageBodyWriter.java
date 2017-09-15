package org.dotwebstack.framework.frontend.http.provider.graph;

import javax.ws.rs.Produces;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.MediaTypes;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Service;

@Service
@SparqlProvider(resultType = ResultType.GRAPH)
@Produces(MediaTypes.LDJSON)
public class JsonLdGraphMessageBodyWriter extends GraphMessageBodyWriter {

  JsonLdGraphMessageBodyWriter() {
    super(RDFFormat.JSONLD);
  }

}
