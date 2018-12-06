package org.dotwebstack.framework.frontend.ld.writer.tuple;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.springframework.stereotype.Service;

@Service
@EntityWriter(resultType = ResultType.TUPLE)
@Produces({"text/html"})
public class HtmlTupleEntityWriter {

  private final List<MediaType> mediaTypes = Collections.singletonList(MediaTypes.TEXT_HTML_TYPE);
}

