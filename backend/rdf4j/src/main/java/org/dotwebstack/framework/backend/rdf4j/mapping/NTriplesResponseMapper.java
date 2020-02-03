package org.dotwebstack.framework.backend.rdf4j.mapping;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
public class NTriplesResponseMapper extends BaseResponseMapper {

  static final MimeType N_TRIPLES_MEDIA_TYPE = MimeType.valueOf("application/n-triples");

  @Override
  MimeType supportedMimeType() {
    return N_TRIPLES_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.NTRIPLES;
  }
}
