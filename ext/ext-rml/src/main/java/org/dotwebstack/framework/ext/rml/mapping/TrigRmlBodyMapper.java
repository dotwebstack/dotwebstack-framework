package org.dotwebstack.framework.ext.rml.mapping;

import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import com.taxonic.carml.model.TriplesMap;
import graphql.GraphQL;
import io.swagger.v3.oas.models.Operation;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class TrigRmlBodyMapper extends AbstractRmlBodyMapper {

  static final MediaType TRIG_MEDIA_TYPE = MediaType.parseMediaType("application/trig");

  TrigRmlBodyMapper(GraphQL graphQL, RdfRmlMapper rmlMapper, Map<Operation, Set<TriplesMap>> mappingsPerOperation,
      Set<Namespace> namespaces) {
    super(graphQL, rmlMapper, mappingsPerOperation, namespaces);
  }

  @Override
  MediaType supportedMediaType() {
    return TRIG_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.TRIG;
  }
}
