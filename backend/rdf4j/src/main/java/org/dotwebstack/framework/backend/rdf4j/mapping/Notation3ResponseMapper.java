package org.dotwebstack.framework.backend.rdf4j.mapping;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
public class Notation3ResponseMapper extends BaseResponseMapper {

  static final MimeType N3_MEDIA_TYPE = MimeType.valueOf("text/n3");

  @Override
  MimeType supportedMimeType() {
    return N3_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.N3;
  }
}
