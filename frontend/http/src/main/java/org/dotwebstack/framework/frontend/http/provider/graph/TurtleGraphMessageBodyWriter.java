package org.dotwebstack.framework.frontend.http.provider.graph;

import javax.ws.rs.Produces;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.MediaTypes;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SparqlProvider(resultType = ResultType.GRAPH)
@Produces(MediaTypes.TURTLE)
public class TurtleGraphMessageBodyWriter extends GraphMessageBodyWriter {

  @Autowired
  public TurtleGraphMessageBodyWriter() {
    super(RDFFormat.TURTLE);
  }

}
