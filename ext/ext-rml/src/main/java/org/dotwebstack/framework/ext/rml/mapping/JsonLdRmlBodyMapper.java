package org.dotwebstack.framework.ext.rml.mapping;

import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import com.taxonic.carml.model.TriplesMap;
import graphql.GraphQL;
import io.swagger.v3.oas.models.Operation;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class JsonLdRmlBodyMapper extends AbstractRmlBodyMapper {

  static final MediaType JSON_LD_MEDIA_TYPE = MediaType.parseMediaType("application/ld+json");

  private final WriterConfig jsonLdSettings;

  JsonLdRmlBodyMapper(GraphQL graphQL, RdfRmlMapper rmlMapper, Map<Operation, Set<TriplesMap>> mappingsPerOperation,
      Set<Namespace> namespaces) {
    super(graphQL, rmlMapper, mappingsPerOperation, namespaces);

    WriterConfig writerConfig = new WriterConfig();
    writerConfig.set(JSONLDSettings.HIERARCHICAL_VIEW, true);
    writerConfig.set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
    writerConfig.set(JSONLDSettings.OPTIMIZE, true);
    jsonLdSettings = writerConfig;
  }

  @Override
  MediaType supportedMediaType() {
    return JSON_LD_MEDIA_TYPE;
  }

  @Override
  RDFFormat rdfFormat() {
    return RDFFormat.JSONLD;
  }

  @Override
  WriterConfig getWriterConfig() {
    return jsonLdSettings;
  }
}
