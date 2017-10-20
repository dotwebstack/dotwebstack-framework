package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GetRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(GetRequestHandler.class);

  private final Operation operation;

  private final InformationProduct informationProduct;

  private final Map<MediaType, Property> schemaMap;

  public GetRequestHandler(@NonNull Operation operation,
      @NonNull InformationProduct informationProduct, @NonNull Map<MediaType, Property> schemaMap) {
    this.operation = operation;
    this.informationProduct = informationProduct;
    this.schemaMap = schemaMap;
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }

  public Map<MediaType, Property> getSchemaMap() {
    return schemaMap;
  }

  @Override
  public Response apply(@NonNull ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();
    LOG.debug("Handling GET request for path {}", path);

    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      String name = getParameterName();
      String value = name != null ? getParameterValue(containerRequestContext, name) : null;

      TupleQueryResult result = (TupleQueryResult) informationProduct.getResult(value);
      TupleEntity entity = new TupleEntity(schemaMap, result);

      return Response.ok(entity).build();
    }

    return Response.serverError().build();
  }

  private String getParameterName() {
    for (Parameter parameter : operation.getParameters()) {
      LOG.debug("Vendor extensions for parameter '{}': {}", parameter.getName(),
          parameter.getVendorExtensions());

      // TODO What if parameter does not have the vendor extension?
      // TODO What if vendor extension does not equal filter ID?

      String filterId =
          informationProduct.getFilters().iterator().next().getIdentifier().toString();

      if (parameter.getVendorExtensions().get(OpenApiSpecificationExtensions.FILTER_INPUT).equals(
          filterId)) {
        String name = parameter.getName();

        LOG.debug("Filter for parameter '{}': {}", parameter.getName(), filterId);

        return name;
      }
    }

    return null;
  }

  private static String getParameterValue(ContainerRequestContext context, String name) {
    // TODO Take into account the parameter in (type)

    String value = context.getUriInfo().getPathParameters().getFirst(name);

    LOG.debug("Value for parameter '{}': '{}'", name, value);

    return value;
  }

}

