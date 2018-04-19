package org.dotwebstack.framework.frontend.openapi.handlers;

import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.model.TriplesMap;
import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.transaction.RmlMappingResourceProvider;
import org.eclipse.rdf4j.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class TransactionBodyMapper {

  private static final Logger LOG = LoggerFactory.getLogger(TransactionBodyMapper.class);

  private RmlMappingResourceProvider rmlMappingResourceProvider;

  @Autowired
  public TransactionBodyMapper(@NonNull RmlMappingResourceProvider rmlMappingResourceProvider) {
    this.rmlMappingResourceProvider = rmlMappingResourceProvider;
  }

  Model map(@NonNull Operation operation, @NonNull RequestParameters requestParameters) {
    for (io.swagger.models.parameters.Parameter openApiParameter : operation.getParameters()) {

      if (openApiParameter instanceof BodyParameter) {
        Map<String, Object> vendorExtensions = openApiParameter.getVendorExtensions();

        LOG.debug("Vendor extensions for body parameter '{}': {}", openApiParameter.getName(),
            vendorExtensions);

        String rmlMapping = (String)vendorExtensions.get(
            OpenApiSpecificationExtensions.RML_MAPPING);

        if (rmlMapping == null) {
          throw new RequestHandlerRuntimeException(
              "Vendor extension x-dotwebstack-rml-mapping not found for body parameter");
        }

        String value = requestParameters.getRawBody();

        Set<TriplesMap> mapping = RmlCustomMappingLoader.build().load(
              rmlMappingResourceProvider.get(rmlMapping).getModel());

        RmlMapper mapper = RmlMapper.newBuilder().build();

        InputStream inputStream = IOUtils.toInputStream(value);
        mapper.bindInputStream("stream-A", inputStream);

        return mapper.map(mapping);
      }
    }

    throw new RequestHandlerRuntimeException("body parameter not found");
  }

}

