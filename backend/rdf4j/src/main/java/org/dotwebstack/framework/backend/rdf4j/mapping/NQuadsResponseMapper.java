package org.dotwebstack.framework.backend.rdf4j.mapping;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.util.MimeType;

public class NQuadsResponseMapper extends BaseResponseMapper {

  static final MimeType N_QUADS_MEDIA_TYPE = MimeType.valueOf("application/n-quads");

  @Override
  MimeType supportedMimeType() {
    return N_QUADS_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.NQUADS;
  }
}
