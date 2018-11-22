package org.dotwebstack.framework.frontend.ld.writer.graph;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Service;

@Service
@EntityWriter(resultType = ResultType.GRAPH)
@Produces({MediaTypes.LDJSON, MediaType.TEXT_HTML})
public class HtmlGraphEntityWriter extends GraphEntityWriter {

  HtmlGraphEntityWriter() {
    super(RDFFormat.TURTLE, MediaTypes.TEXT_HTML_TYPE);
  }

}
