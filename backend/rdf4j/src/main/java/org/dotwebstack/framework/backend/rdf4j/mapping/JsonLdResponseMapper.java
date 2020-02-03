package org.dotwebstack.framework.backend.rdf4j.mapping;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
public class JsonLdResponseMapper extends BaseResponseMapper {

  static final MimeType JSON_LD_MEDIA_TYPE = MimeType.valueOf("application/ld+json");

  @Override
  MimeType supportedMimeType() {
    return JSON_LD_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.JSONLD;
  }
}
