package org.dotwebstack.framework.frontend.openapi.handlers;

import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.JsonPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.vocab.Rdf;
import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.BadRequestException;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.rml.RmlMapping;
import org.dotwebstack.framework.rml.RmlMappingResourceProvider;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class TransactionRequestBodyMapper {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionRequestBodyMapper.class);
  private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();
  private RmlMappingResourceProvider rmlMappingResourceProvider;

  @Autowired
  public TransactionRequestBodyMapper(
      @NonNull RmlMappingResourceProvider rmlMappingResourceProvider) {
    this.rmlMappingResourceProvider = rmlMappingResourceProvider;
  }

  Model map(@NonNull Operation operation, @NonNull RequestParameters requestParameters) {
    for (Parameter openApiParameter : operation.getParameters()) {

      if (openApiParameter instanceof BodyParameter) {
        Map<String, Object> vendorExtensions = openApiParameter.getVendorExtensions();

        LOG.debug("Vendor extensions for body parameter '{}': {}", openApiParameter.getName(),
            vendorExtensions);

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

    throw new RequestHandlerRuntimeException("Body parameter not found");
  }

}

