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
class ParameterNameToSwaggerParameterValueMapper {

  private static final Logger LOG = LoggerFactory.getLogger(GetRequestHandler.class);

  private static ValueFactory valueFactory = SimpleValueFactory.getInstance();

  Map<String, String> map(@NonNull Operation operation, @NonNull InformationProduct product,
      @NonNull ContainerRequestContext context) {
    Map<String, String> result = new HashMap<>();

    for (io.swagger.models.parameters.Parameter swaggerParameter : operation.getParameters()) {
      Map<String, Object> vendorExtensions = swaggerParameter.getVendorExtensions();

      LOG.debug("Vendor extensions for parameter '{}': {}", swaggerParameter.getName(),
          vendorExtensions);

      Object parameterIdString =
          vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER_INPUT);

      if (parameterIdString == null) {
        // Vendor extension x-dotwebstack-parameter-input not found for parameter
        continue;
      }

      IRI parameterId = valueFactory.createIRI((String) parameterIdString);
      Parameter parameter = getParameter(product, parameterId);

      if (parameter == null) {
        throw new ConfigurationException(String.format(
            "No parameter found for vendor extension value: '%s'", parameterIdString));
      }

      String value = getSwaggerParameterValue(context, swaggerParameter);

      result.put(parameter.getName(), value);
    }

    return result;
  }

  private static Parameter getParameter(InformationProduct product, IRI iri) {
    for (Parameter parameter : product.getParameters()) {
      if (parameter.getIdentifier().equals(iri)) {
        return parameter;
      }
    }

    return null;
  }

  private static String getSwaggerParameterValue(ContainerRequestContext context,
      io.swagger.models.parameters.Parameter parameter) {
    switch (parameter.getIn()) {
      case "header":
        return context.getHeaders().getFirst(parameter.getName());
      case "path":
        return context.getUriInfo().getPathParameters().getFirst(parameter.getName());
      case "query":
        return context.getUriInfo().getQueryParameters().getFirst(parameter.getName());
      default:
        throw new ConfigurationException(
            String.format("Unknown parameter location: '%s'", parameter.getIn()));
    }
  }

}
