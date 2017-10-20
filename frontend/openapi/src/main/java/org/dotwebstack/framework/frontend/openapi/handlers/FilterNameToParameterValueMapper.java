package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class FilterNameToParameterValueMapper {

  private static final Logger LOG = LoggerFactory.getLogger(GetRequestHandler.class);

  private static ValueFactory valueFactory = SimpleValueFactory.getInstance();

  Map<String, String> map(@NonNull Operation operation, @NonNull InformationProduct product,
      @NonNull ContainerRequestContext context) {
    Map<String, String> result = new HashMap<>();

    for (Parameter parameter : operation.getParameters()) {
      Map<String, Object> vendorExtensions = parameter.getVendorExtensions();

      LOG.debug("Vendor extensions for parameter '{}': {}", parameter.getName(), vendorExtensions);

      Object filterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.FILTER_INPUT);

      if (filterIdString == null) {
        // Vendor extension x-dotwebstack-filter-input not found for parameter
        continue;
      }

      IRI filterId = valueFactory.createIRI((String) filterIdString);
      Filter filter = getFilter(product, filterId);

      if (filter == null) {
        throw new ConfigurationException(
            String.format("No filter found for vendor extension value: '%s'", filterIdString));
      }

      LOG.debug("Filter for parameter '{}': <{}>", parameter.getName(), filter.getIdentifier());

      String value = getParameterValue(context, parameter);
      LOG.debug("Value for parameter '{}': <{}>", parameter.getName(), value);

      result.put(filter.getName(), value);
    }

    return result;
  }

  private Filter getFilter(InformationProduct product, IRI iri) {
    for (Filter filter : product.getFilters()) {
      if (filter.getIdentifier().equals(iri)) {
        return filter;
      }
    }

    return null;
  }

  private static String getParameterValue(ContainerRequestContext context, Parameter parameter) {
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
