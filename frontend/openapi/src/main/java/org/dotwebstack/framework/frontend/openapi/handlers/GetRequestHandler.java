package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.Operation;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
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
  public Response apply(@NonNull ContainerRequestContext context) {
    String path = context.getUriInfo().getPath();
    LOG.debug("Handling GET request for path {}", path);

    if (ResultType.TUPLE.equals(informationProduct.getResultType())) {
      Map<String, String> values =
          new FilterNameToParameterValueMapper().map(operation, informationProduct, context);

      TupleQueryResult result = (TupleQueryResult) informationProduct.getResult(values);
      TupleEntity entity = new TupleEntity(schemaMap, result);

      return Response.ok(entity).build();
    }

    return Response.serverError().build();
  }

}

