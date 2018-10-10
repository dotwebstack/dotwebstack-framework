package org.dotwebstack.framework.frontend.openapi.mappers;

import com.atlassian.oai.validator.model.ApiOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response.Status;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestHandlerFactory;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InformationProductRequestMapper implements RequestMapper {

  private static final Logger LOG = LoggerFactory.getLogger(InformationProductRequestMapper.class);

  private final InformationProductResourceProvider informationProductResourceProvider;

  private final RequestHandlerFactory requestHandlerFactory;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Autowired
  public InformationProductRequestMapper(
      @NonNull InformationProductResourceProvider informationProductLoader,
      @NonNull RequestHandlerFactory requestHandlerFactory) {
    this.informationProductResourceProvider = informationProductLoader;
    this.requestHandlerFactory = requestHandlerFactory;
  }

  @Override
  public Boolean supportsVendorExtension(Map<String, Object> vendorExtensions) {
    return vendorExtensions != null
        && vendorExtensions.keySet().stream()
        .anyMatch(key -> key.equals(OpenApiSpecificationExtensions.INFORMATION_PRODUCT));
  }

  @Override
  public void map(Resource.Builder resourceBuilder, OpenAPI openApi, ApiOperation apiOperation,
      Operation getOperation, String absolutePath) {
    String okStatusCode = Integer.toString(Status.OK.getStatusCode());

    if (getOperation.getResponses() == null
        || !getOperation.getResponses().containsKey(okStatusCode)) {
      throw new ConfigurationException(String.format(
          "Resource '%s' does not specify a status %s response.", absolutePath, okStatusCode));
    }

    Set<String> produces =
        getOperation.getResponses() != null
            ? getOperation.getResponses().get(okStatusCode).getContent().keySet()
            : null;

    if (produces == null) {
      throw new ConfigurationException(
          String.format("Path '%s' should produce at least one media type.", absolutePath));
    }

    ApiResponse response = getOperation.getResponses().get(okStatusCode);

    IRI informationProductIdentifier =
        valueFactory.createIRI((String) getOperation.getExtensions().get(
            OpenApiSpecificationExtensions.INFORMATION_PRODUCT));

    InformationProduct informationProduct =
        informationProductResourceProvider.get(informationProductIdentifier);

    ResourceMethod.Builder methodBuilder =
        resourceBuilder.addMethod(apiOperation.getMethod().name()).handledBy(
            requestHandlerFactory.newInformationProductRequestHandler(apiOperation,
                informationProduct, response, openApi));

    produces.forEach(methodBuilder::produces);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Mapped {} operation for request path {}", apiOperation.getMethod().name(),
          absolutePath);
    }
  }

}
