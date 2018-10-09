package org.dotwebstack.framework.frontend.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.servers.Server;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.http.client.utils.URIBuilder;
import org.glassfish.jersey.server.ContainerRequest;

/**
 * Class to create the BaseUri, used in relative links.
 */
@UtilityClass
public class BaseUriFactory {

  /**
   * Constructs a URI: {@code <scheme>://<host>[:<port>]/<basePath>}<br />
   * <br />
   *
   * If the given operation has 'servers' defined, the base URI is the 'url' of the first server
   * entry for the operation.
   *
   * Otherwise, the base URI is the 'url' of the first server entry for the Open API Specification.
   *
   * If {@code X-Forwarded-Host}
   *
   * Where:
   *
   * <ul>
   * <li>{@code <scheme>} and {@code <basePath} are taken from the Open API Spec. If the given
   * operation has 'servers' defined, these are taken from the 'url' of the first server, otherwise
   * these are taken from the 'url' of the first server entry fro the OpenAPI specification.</li>
   * <li>{@code <host>[:<port>]} The host, and optional port number are taken from the
   * {@code X-Forwarded-Host} header or from the request. If the {@code X-Forwarded-Host} header is
   * set, it's first value is used as host and port combination. If the header is not set, the host
   * and port number are taken from the request.</li>
   * </ul>
   *
   * @return The base uri for the give request and OpenAPI spec.
   */
  public static String determineBaseUri(@NonNull ContainerRequest containerRequest,
      @NonNull OpenAPI openAPI, @NonNull Operation operation) {
    String openApiSpecUri = openAPI.getServers().get(0).getUrl();

    if (operation.getServers() != null) {
      Server operationServer = operation.getServers().get(0);
      if (operationServer != null) {
        openApiSpecUri = operationServer.getUrl();
      }
    }

    try {
      return new URIBuilder(openApiSpecUri).setHost(getHost(containerRequest)).build().toString();
    } catch (URISyntaxException use) {
      throw new IllegalStateException("BaseUri could not be constructed", use);
    }
  }

  /**
   * <p>
   * Returns the host and optional port number for the base uri.
   * </p>
   * <p>
   * <strong>Note:</strong> The port number is considered part of the host, as per the HTTP/1.1
   * spec. See <a href="https://tools.ietf.org/html/rfc7230#section-5.4">IETF RFC7230, section 5.4,
   * Host</a>.
   * </p>
   *
   * @return The value of the {@code X-Forwarded-Host} header, if set. Otherwise a combination of
   *         {@code <host>[:<port>]} as present on the containerRequest.
   */
  private static String getHost(ContainerRequest containerRequest) {
    return getForwardedHostFromRequestHeader(containerRequest).orElse(
        getHostFromRequest(containerRequest));
  }

  private static Optional<String> getForwardedHostFromRequestHeader(
      ContainerRequest containerRequest) {
    return Optional.ofNullable(
        containerRequest.getRequestHeaders().getFirst("x-forwarded-host")).map(
            header -> header.split(",")[0]);
  }

  private static String getHostFromRequest(ContainerRequest containerRequest) {
    URI baseUri = containerRequest.getBaseUri();
    String host = baseUri.getHost();
    int port = baseUri.getPort();
    if (port != -1) {
      host += ":" + port;
    }
    return host;
  }

}
