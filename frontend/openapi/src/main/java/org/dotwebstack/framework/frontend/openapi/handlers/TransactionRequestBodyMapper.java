package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.ImmutableMap;
import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.vocab.Rdf;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.RequestBody;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.BadRequestException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.rml.RmlMapping;
import org.dotwebstack.framework.rml.RmlMappingResourceProvider;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class TransactionRequestBodyMapper {

  private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();
  private RmlMappingResourceProvider rmlMappingResourceProvider;

  @Autowired
  public TransactionRequestBodyMapper(
      @NonNull RmlMappingResourceProvider rmlMappingResourceProvider) {
    this.rmlMappingResourceProvider = rmlMappingResourceProvider;
  }

  Model map(@NonNull Operation operation, @NonNull RequestParameters requestParameters) {
    RequestBody requestBody = operation.getRequestBody();
    if (requestBody == null) {
      throw new RequestHandlerRuntimeException("Body parameter not found");
    }
    Map<String, Object> vendorExtensions = requestBody.getExtensions();

    LOG.debug("Vendor extensions for body parameter '{}': {}", requestBody.getDescription(),
        vendorExtensions);

    if (vendorExtensions == null) {
      vendorExtensions = ImmutableMap.of();
    }
    String rmlMappingName =
        (String) vendorExtensions.get(OpenApiSpecificationExtensions.RML_MAPPING);

    if (rmlMappingName == null) {
      throw new RequestHandlerRuntimeException(
          "Vendor extension x-dotwebstack-rml-mapping not found for body parameter");
    }

    String value = requestParameters.getRawBody();

    if (value == null) {
      throw new BadRequestException("Body is empty");
    }

    RmlMapping rmlMapping =
        rmlMappingResourceProvider.get(valueFactory.createIRI(rmlMappingName));
    Set<TriplesMap> mapping = RmlCustomMappingLoader.build().load(rmlMapping.getModel());

    RmlMapper mapper = RmlMapper.newBuilder().setLogicalSourceResolver(Rdf.Ql.JsonPath,
        new JsonPathResolver()).build();

    InputStream inputStream = IOUtils.toInputStream(value);
    mapper.bindInputStream(rmlMapping.getStreamName(), inputStream);

    LOG.debug("Apply rml mapping: {}", mapping);

    return mapper.map(mapping);
  }

}

