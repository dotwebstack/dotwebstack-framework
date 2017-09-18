package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.properties.Property;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.dotwebstack.framework.frontend.openapi.entity.Entity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GetRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(GetRequestHandler.class);

  private InformationProduct informationProduct;

  private Map<String, Property> schemaMap;

  public GetRequestHandler(InformationProduct informationProduct, Map<String, Property> schemaMap) {
    this.informationProduct = Objects.requireNonNull(informationProduct);
    this.schemaMap = Objects.requireNonNull(schemaMap);
  }

  public InformationProduct getInformationProduct() {
    return informationProduct;
  }

  public Map<String, Property> getSchemaMap() {
    return schemaMap;
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();
    LOG.debug("Handling GET request for path {}", path);

    Entity entity = new Entity(informationProduct.getResult(), schemaMap);

    return Response.ok(entity).build();
  }

}
