package org.dotwebstack.framework.frontend.ld.handlers;

import com.google.common.collect.ImmutableMap;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
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

    Object result = representation.getInformationProduct().getResult(ImmutableMap.of());
    if (result instanceof GraphQueryResult) {
      return Response.ok(new GraphEntity((GraphQueryResult) result, representation)).build();
    }
    if (result instanceof TupleQueryResult) {
      return Response.ok(new TupleEntity((TupleQueryResult) result, representation)).build();
    }
    throw new IllegalStateException("Received a query result that was not supported: " + result);
  }
}
