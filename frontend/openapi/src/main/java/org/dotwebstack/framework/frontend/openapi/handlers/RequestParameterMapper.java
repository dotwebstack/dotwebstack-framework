package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private static Parameter<?> getParameter(InformationProduct product, IRI iri) {
    for (Parameter<?> parameter : product.getParameters()) {
      if (parameter.getIdentifier().equals(iri)) {
        return parameter;
      }
    }

    return null;
  }

  Map<String, String> map(@NonNull Operation operation, @NonNull InformationProduct product,
      @NonNull RequestParameters requestParameters) {
    Map<String, String> result = new HashMap<>();

    for (io.swagger.models.parameters.Parameter openApiParameter : operation.getParameters()) {

      if (openApiParameter instanceof BodyParameter) {
        Collection<Property> properties =
            ((BodyParameter) openApiParameter).getSchema().getProperties().values();
        for (Property property : properties) {
          Map<String, Object> vendorExtensions = property.getVendorExtensions();

          LOG.debug("Vendor extensions for property in parameter '{}': {}", property.getName(),
              openApiParameter.getName(), vendorExtensions);

          Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

          if (parameterIdString == null) {
            // XXX (PvH) Case wordt niet gedekt
            // Vendor extension x-dotwebstack-parameter not found for property
            continue;
          }

          fillResult((String) parameterIdString, product, requestParameters, result);
        }
      } else {
        Map<String, Object> vendorExtensions = openApiParameter.getVendorExtensions();

        LOG.debug("Vendor extensions for parameter '{}': {}", openApiParameter.getName(),
            vendorExtensions);

        Object parameterIdString = vendorExtensions.get(OpenApiSpecificationExtensions.PARAMETER);

        if (parameterIdString == null) {
          // Vendor extension x-dotwebstack-parameter not found for property
          continue;
        }

        fillResult((String) parameterIdString, product, requestParameters, result);
      }
    }
    return result;
  }

  // XXX (PvH) Het is een beetje een bad practice om parameters (in dit geval de result Map) te
  // manipuleren. Dit komt de eenvoud en het overzicht niet ten goede.
  // Ik zou in dit geval enkel de code tussen // 1 en // 2 in een aparte method stoppen (de huidige
  // getParameter methode)
  private void fillResult(String parameterIdString, InformationProduct product,
      RequestParameters requestParameters, Map<String, String> result) {

    // 1
    IRI parameterId = VALUE_FACTORY.createIRI((String) parameterIdString);
    Parameter<?> parameter = getParameter(product, parameterId);

    if (parameter == null) {
      throw new ConfigurationException(
          String.format("No parameter found for vendor extension value: '%s'", parameterIdString));
    }
    // 2

    String value = requestParameters.get(parameter.getName());

    result.put(parameter.getName(), value);
  }
}

