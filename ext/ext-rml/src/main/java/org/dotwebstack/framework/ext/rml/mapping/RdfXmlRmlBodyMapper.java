package org.dotwebstack.framework.ext.rml.mapping;

import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import com.taxonic.carml.model.TriplesMap;
import io.swagger.v3.oas.models.Operation;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class RdfXmlRmlBodyMapper extends AbstractRmlBodyMapper {

  static final MediaType RDF_XML_MEDIA_TYPE = MediaType.parseMediaType("application/rdf+xml");

  RdfXmlRmlBodyMapper(RdfRmlMapper rmlMapper, Map<Operation, Set<TriplesMap>> mappingsPerOperation,
      Set<Namespace> namespaces) {
    super(rmlMapper, mappingsPerOperation, namespaces);
  }

  @Override
  MediaType supportedMediaType() {
    return RDF_XML_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.RDFXML;
  }
}
