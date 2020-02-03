package org.dotwebstack.framework.backend.rdf4j.mapping;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
public class RdfXmlResponseMapper extends BaseResponseMapper {

  static final MimeType RDF_XML_MEDIA_TYPE = MimeType.valueOf("application/rdf+xml");

  @Override
  MimeType supportedMimeType() {
    return RDF_XML_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.RDFXML;
  }
}
