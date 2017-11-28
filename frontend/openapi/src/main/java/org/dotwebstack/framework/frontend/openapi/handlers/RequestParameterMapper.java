package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class RequestParameterMapper {

  private static final Logger LOG = LoggerFactory.getLogger(RequestParameterMapper.class);

  private static ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private static Parameter<?> getParameter(InformationProduct product, IRI iri) {
    for (Parameter<?> parameter : product.getParameters()) {
      if (parameter.getIdentifier().equals(iri)) {
        return parameter;
      }
    }

    return null;
  }

  private static String getOpenApiParameterValue(ContainerRequestContext context,
      io.swagger.models.parameters.Parameter openApiParameter) {
    switch (openApiParameter.getIn()) {
      case "header":
        return context.getHeaders().getFirst(openApiParameter.getName());
      case "path":
        return context.getUriInfo().getPathParameters().getFirst(openApiParameter.getName());
      case "query":
        return context.getUriInfo().getQueryParameters().getFirst(openApiParameter.getName());
      default:
        throw new ConfigurationException(
            String.format("Unknown parameter location: '%s'", openApiParameter.getIn()));
    }
  }

  Map<String, Object> map(@NonNull Operation operation, @NonNull InformationProduct product,
      @NonNull ContainerRequestContext context) {
    Map<String, Object> result = new HashMap<>();

    for (io.swagger.models.parameters.Parameter openApiParameter : operation.getParameters()) {
      Map<String, Object> vendorExtensions = openApiParameter.getVendorExtensions();

      LOG.debug("Vendor extensions for parameter '{}': {}", openApiParameter.getName(),
          vendorExtensions);

      Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

      if (parameterIdString == null) {
        // Vendor extension x-dotwebstack-parameter not found for parameter
        continue;
      }

      IRI parameterId = valueFactory.createIRI((String) parameterIdString);
      Parameter<?> parameter = getParameter(product, parameterId);

      if (parameter == null) {
        throw new ConfigurationException(String.format(
            "No parameter found for vendor extension value: '%s'", parameterIdString));
      }

      String value = getOpenApiParameterValue(context, openApiParameter);

      result.put(parameter.getName(), value);
    }

    return result;
  }

}
