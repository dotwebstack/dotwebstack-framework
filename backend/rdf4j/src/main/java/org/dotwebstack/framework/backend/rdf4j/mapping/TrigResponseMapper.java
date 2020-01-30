package org.dotwebstack.framework.backend.rdf4j.mapping;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.util.MimeType;

public class TrigResponseMapper extends BaseResponseMapper {

  static final MimeType TRIG_MEDIA_TYPE = MimeType.valueOf("application/trig");

  @Override
  MimeType supportedMimeType() {
    return TRIG_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.TRIG;
  }
}
