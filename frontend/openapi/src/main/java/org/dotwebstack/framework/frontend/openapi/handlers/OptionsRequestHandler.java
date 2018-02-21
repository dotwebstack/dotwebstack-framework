package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.Sets;
import io.swagger.models.HttpMethod;
import io.swagger.models.Path;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.process.Inflector;

public class OptionsRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private final Path path;

  public OptionsRequestHandler(Path path) {
    this.path = path;
  }

  @Override
  public Response apply(ContainerRequestContext containerRequestContext) {
    containerRequestContext.setProperty("path", path);

    Set<HttpMethod> allowedMethods = Sets.newHashSet(path.getOperationMap().keySet());

    if (!allowedMethods.contains(HttpMethod.HEAD)) {
      allowedMethods.add(HttpMethod.HEAD);
    }

    if (!allowedMethods.contains(HttpMethod.OPTIONS)) {
      allowedMethods.add(HttpMethod.OPTIONS);
    }

    Set<String> allowHeader =
        allowedMethods.stream().map(Object::toString).collect(Collectors.toSet());

    return Response.ok().allow(allowHeader).build();
  }

}
