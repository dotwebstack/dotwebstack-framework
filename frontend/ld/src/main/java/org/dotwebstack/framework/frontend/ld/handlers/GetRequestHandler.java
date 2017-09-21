package org.dotwebstack.framework.frontend.ld.handlers;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.glassfish.jersey.process.Inflector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private static final Logger LOG = LoggerFactory.getLogger(GetRequestHandler.class);

  private Representation representation;

  public GetRequestHandler(@NonNull Representation representation) {
    this.representation = representation;
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    String path = containerRequestContext.getUriInfo().getPath();
    LOG.debug("Handling GET request for path {}", path);

    Object result = representation.getInformationProduct().getResult();
    return Response.ok(result).build();
  }
}
