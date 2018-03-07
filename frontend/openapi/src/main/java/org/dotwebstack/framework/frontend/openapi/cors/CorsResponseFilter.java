package org.dotwebstack.framework.frontend.openapi.cors;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import java.io.IOException;
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
    if (!requestContext.getHeaders().containsKey(HttpHeaders.ORIGIN)) {
      return;
    }

    if (javax.ws.rs.HttpMethod.OPTIONS.equals(requestContext.getMethod())) {
      handlePreflightRequest(requestContext, responseContext);
    } else {
      handleActualRequest(requestContext, responseContext);
    }
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

    if (actualRequestMethod == null) {
      return;
    }

    Set<HttpMethod> allowedMethods = Sets.newHashSet(path.getOperationMap().keySet());
    allowedMethods.add(HttpMethod.HEAD);
    allowedMethods.add(HttpMethod.OPTIONS);

    if (!allowedMethods.contains(HttpMethod.valueOf(actualRequestMethod))) {
      return;
    }

    // Validate "Access-Control-Request-Headers" header

    String expectedHeadersStr =
        requestContext.getHeaders().getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);

    if (expectedHeadersStr == null) {
      expectedHeadersStr = "";
    }

    Set<String> expectedHeaders = ImmutableSet.copyOf(
        Splitter.on(",").trimResults().omitEmptyStrings().split(expectedHeadersStr));

    expectedHeaders = expectedHeaders.stream().map(String::toLowerCase).collect(Collectors.toSet());

    List<Parameter> requestParameters =
        path.getOperationMap().get(HttpMethod.valueOf(actualRequestMethod)).getParameters();

    Set<String> allowedHeaders =
        requestParameters.stream().filter(p -> "header".equals(p.getIn())).map(
            Parameter::getName).collect(Collectors.toSet());

    allowedHeaders = allowedHeaders.stream().map(String::toLowerCase).collect(Collectors.toSet());

    if (!expectedHeaders.isEmpty()) {
      Set<String> diff = Sets.difference(expectedHeaders, allowedHeaders);

      if (!diff.isEmpty()) {
        return;
      }
    }

    // Add CORS headers

    responseContext.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
        requestContext.getHeaders().getFirst(HttpHeaders.ORIGIN));

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

    responseContext.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
        requestContext.getHeaders().getFirst(HttpHeaders.ORIGIN));

    Map<String, Property> responseHeaders = operation.getResponses().get(statusCode).getHeaders();

    if (responseHeaders != null && responseHeaders.size() > 0) {
      Set<String> exposedHeaders =
          responseHeaders.keySet().stream().map(String::toLowerCase).collect(Collectors.toSet());
      responseContext.getHeaders().add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
          Joiner.on(", ").join(exposedHeaders));
    }
  }

}
