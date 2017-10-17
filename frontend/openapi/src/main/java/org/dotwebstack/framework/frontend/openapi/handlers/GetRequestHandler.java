package org.dotwebstack.framework.frontend.openapi.handlers;

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

  private InformationProduct informationProduct;

  private Map<MediaType, Property> schemaMap;

  public GetRequestHandler(@NonNull InformationProduct informationProduct,
      @NonNull Map<MediaType, Property> schemaMap) {
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
      
      // Extract parameter map from request, i.e.:
      // id=NL.IMRO.0005.BPZW13BEHE1-OW01
      // Reuse code in grid-api
      
      TupleQueryResult result = (TupleQueryResult) informationProduct.getResult();
      TupleEntity entity = new TupleEntity(schemaMap, result);
      return Response.ok(entity).build();
    }

    return Response.serverError().build();
  }

}
