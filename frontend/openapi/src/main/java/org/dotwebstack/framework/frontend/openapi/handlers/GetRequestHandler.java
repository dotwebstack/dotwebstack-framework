package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.models.properties.Property;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.openapi.EntityBuilder;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(GetRequestHandler.class);

  private InformationProduct informationProduct;

  private EntityBuilder<Object> entityBuilder;

  private Property schema;

  public GetRequestHandler(@NonNull InformationProduct informationProduct,
      @NonNull EntityBuilder<Object> entityBuilder, @NonNull Property schema) {
    this.informationProduct = informationProduct;
    this.entityBuilder = entityBuilder;
    this.schema = schema;
  }

  @Override
  public Response apply(@NonNull ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();
    LOG.debug("Handling GET request for path {}", path);

    Object entity = entityBuilder.build(informationProduct.getResult(), schema);

    return Response.ok(entity).build();
  }

}
