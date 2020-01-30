package org.dotwebstack.framework.backend.rdf4j.mapping;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.util.MimeType;

public class TurtleResponseMapper extends BaseResponseMapper {

  static final MimeType TURTLE_MEDIA_TYPE = MimeType.valueOf("text/turtle");

  @Override
  MimeType supportedMimeType() {
    return TURTLE_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.TURTLE;
  }
}
