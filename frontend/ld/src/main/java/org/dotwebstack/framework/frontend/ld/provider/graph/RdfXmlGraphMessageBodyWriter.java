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
@Produces({MediaTypes.RDFXML, MediaType.APPLICATION_XML})
public class RdfXmlGraphMessageBodyWriter extends GraphMessageBodyWriter {

  RdfXmlGraphMessageBodyWriter() {
    super(RDFFormat.RDFXML, MediaTypes.RDFXML_TYPE, MediaType.APPLICATION_XML_TYPE);
  }

}
