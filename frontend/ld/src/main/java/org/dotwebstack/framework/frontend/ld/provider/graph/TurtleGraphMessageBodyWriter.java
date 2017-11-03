package org.dotwebstack.framework.frontend.ld.provider.graph;

import javax.ws.rs.Produces;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.ld.provider.MediaTypes;
import org.dotwebstack.framework.frontend.ld.provider.SparqlProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Service;

@Service
@SparqlProvider(resultType = ResultType.GRAPH)
@Produces(MediaTypes.TURTLE)
public class TurtleGraphMessageBodyWriter extends GraphMessageBodyWriter {

  TurtleGraphMessageBodyWriter() {
    super(RDFFormat.TURTLE);
  }

}
