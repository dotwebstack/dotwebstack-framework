package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.Sets;
import io.swagger.models.HttpMethod;
import io.swagger.models.Path;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import org.glassfish.jersey.process.Inflector;

public class OptionsRequestHandler implements Inflector<ContainerRequestContext, Response> {

  private final Path path;

  public OptionsRequestHandler(@NonNull Path path) {
    this.path = path;
  }

  @Override
  public Response apply(@NonNull ContainerRequestContext containerRequestContext) {
    containerRequestContext.setProperty(RequestHandlerProperties.PATH, path);

    Set<HttpMethod> allowedMethods = Sets.newHashSet(path.getOperationMap().keySet());
    allowedMethods.add(HttpMethod.HEAD);
    allowedMethods.add(HttpMethod.OPTIONS);

    Set<String> allowHeader =
        allowedMethods.stream().map(Object::toString).collect(Collectors.toSet());

    return Response.ok().allow(allowHeader).build();
  }

}
