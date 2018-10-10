package org.dotwebstack.framework.frontend.openapi.handlers;

import io.swagger.v3.oas.models.PathItem;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.glassfish.jersey.process.Inflector;

public class OptionsRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private final PathItem pathItem;

  public OptionsRequestHandler(@NonNull PathItem pathItem) {
    this.pathItem = pathItem;
  }

  @Override
  public Response apply(@NonNull ContainerRequestContext containerRequestContext) {
    containerRequestContext.setProperty(RequestHandlerProperties.PATH, pathItem);

    // Determine methods from pathItem operations
    Set<String> allowedMethods = pathItem.readOperationsMap() //
        .keySet() //
        .stream() //
        .map(Object::toString) //
        .collect(Collectors.toSet());

    allowedMethods.add(HttpMethod.HEAD);
    allowedMethods.add(HttpMethod.OPTIONS);

    return Response.ok().allow(allowedMethods).build();
  }

}
