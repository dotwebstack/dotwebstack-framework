package org.dotwebstack.framework.frontend.http.provider.graph;

import javax.ws.rs.Produces;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SparqlProvider(resultType = ResultType.GRAPH)
@Produces("application/ld+json")
public class JsonLdGraphMessageBodyWriter extends GraphMessageBodyWriter {

  @Autowired
  public JsonLdGraphMessageBodyWriter() {
    super(RDFFormat.JSONLD);
  }

}
