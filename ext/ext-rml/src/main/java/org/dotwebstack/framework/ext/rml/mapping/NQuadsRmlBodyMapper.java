package org.dotwebstack.framework.ext.rml.mapping;

import graphql.GraphQL;
import io.carml.engine.rdf.RdfRmlMapper;
import io.carml.model.TriplesMap;
import io.swagger.v3.oas.models.Operation;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;

@Component
public class NQuadsRmlBodyMapper extends AbstractRmlBodyMapper {

  static final MediaType N_QUADS_MEDIA_TYPE = MediaType.parseMediaType("application/n-quads");

  NQuadsRmlBodyMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder, GraphQL graphQL, RdfRmlMapper rmlMapper,
      Map<Operation, Set<TriplesMap>> mappingsPerOperation, Set<Namespace> namespaces) {
    super(jackson2ObjectMapperBuilder, graphQL, rmlMapper, mappingsPerOperation, namespaces);
  }

  @Override
  MediaType supportedMediaType() {
    return N_QUADS_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.NQUADS;
  }
}
