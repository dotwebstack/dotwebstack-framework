package org.dotwebstack.framework.frontend.openapi.cors;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;

public class CorsResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(@NonNull ContainerRequestContext requestContext,
      @NonNull ContainerResponseContext responseContext) throws IOException {

    if (javax.ws.rs.HttpMethod.OPTIONS.equals(requestContext.getMethod())) {
      handlePreflightRequest(requestContext, responseContext);
    } else {
      handleActualRequest(requestContext, responseContext);
    }

    // Add this header to every request

    responseContext.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
  }

  private void handlePreflightRequest(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) {
    Path path = (Path) requestContext.getProperty("path");

    if (path == null) {
      return;
    }

    // Validate "Access-Control-Request-Method" header

    String actualRequestMethod =
        requestContext.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);

    String allowHeaderStr = responseContext.getHeaders().getFirst(HttpHeaders.ALLOW).toString();

    Set<HttpMethod> allowedMethods =
        ImmutableSet.copyOf(
            Splitter.on(",").trimResults().omitEmptyStrings().split(allowHeaderStr)).stream().map(
                HttpMethod::valueOf).collect(Collectors.toSet());

    Set<String> allowedHeaders = Collections.emptySet();
    Operation operation = path.getOperationMap().get(
        actualRequestMethod == null ? "" : HttpMethod.valueOf(actualRequestMethod));
    if (operation != null) {
      List<Parameter> requestParameters = operation.getParameters();

      allowedHeaders = requestParameters.stream().filter(p -> "header".equals(p.getIn())).map(
          Parameter::getName).map(String::toLowerCase).collect(Collectors.toSet());
    }

    // Add CORS headers

    responseContext.getHeaders().add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, 86400);

    responseContext.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
        Joiner.on(", ").join(allowedMethods));

    if (!allowedHeaders.isEmpty()) {
      responseContext.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
          Joiner.on(", ").join(allowedHeaders));
    }
  }

  private void handleActualRequest(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) {
    Operation operation = (Operation) requestContext.getProperty("operation");

    if (operation == null) {
      return;
    }

    String statusCode = Integer.toString(responseContext.getStatus());

    if (!operation.getResponses().containsKey(statusCode)) {
      return;
    }

    Map<String, Property> responseHeaders = operation.getResponses().get(statusCode).getHeaders();

    if (responseHeaders != null && responseHeaders.size() > 0) {
      Set<String> exposedHeaders =
          responseHeaders.keySet().stream().map(String::toLowerCase).collect(Collectors.toSet());
      responseContext.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
          Joiner.on(", ").join(exposedHeaders));
    }
  }

}
