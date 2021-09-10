package org.dotwebstack.framework.ext.rml.mapping;

import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import com.taxonic.carml.model.TriplesMap;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

@Component
public class JsonLdRmlResponseMapper extends AbstractRmlResponseMapper {

  static final MimeType JSON_LD_MEDIA_TYPE = MimeType.valueOf("application/ld+json");

  private final WriterConfig jsonLdSettings;

  JsonLdRmlResponseMapper(RdfRmlMapper rmlMapper, Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation,
      Set<Namespace> namespaces) {
    super(rmlMapper, mappingsPerOperation, namespaces);

    WriterConfig writerConfig = new WriterConfig();
    writerConfig.set(JSONLDSettings.HIERARCHICAL_VIEW, true);
    writerConfig.set(JSONLDSettings.JSONLD_MODE, JSONLDMode.COMPACT);
    writerConfig.set(JSONLDSettings.OPTIMIZE, true);
    jsonLdSettings = writerConfig;
  }

  @Override
  MimeType supportedMimeType() {
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
