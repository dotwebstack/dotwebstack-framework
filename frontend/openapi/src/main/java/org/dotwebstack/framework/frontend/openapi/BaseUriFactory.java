package org.dotwebstack.framework.frontend.openapi;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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
   * Where:
   *
   * <ul>
   * <li>{@code <scheme>} is taken from the OpenAPI spec. It is the first scheme listed in the spec.
   * Defaults to {@code https}.</li>
   * <li>{@code <host>[:<port>]} The host, and optional port number are taken from the
   * {@code X-Forwarded-Host} header or from the request. If the {@code X-Forwarded-Host} header is
   * set, it's first value is used as host and port combination. If the header is not set, the host
   * and port number are taken from the request.</li>
   * <li>{@code <basePath>} The base path is taken from the OpenAPI spec.</li>
   * </ul>
   * 
   * @return The base uri for the give request and OpenAPI spec.
   */
  public static String newBaseUri(@NonNull ContainerRequest containerRequest,
      @NonNull Swagger swagger) {
    String baseUri;
    try {
      // @formatter:off
      URI base = new URIBuilder()
          .setScheme(getScheme(swagger))
          .setHost(getHost(containerRequest))
          .setPath(swagger.getBasePath())
          .build();
      // @formatter:on
      baseUri = base.toString();
    } catch (URISyntaxException use) {
      throw new IllegalStateException("BaseUri could not be constructed", use);
    }
    return baseUri;
  }

  /**
   * Returns the scheme for the base uri.
   *
   * @return The first scheme listed in the provided Open API spec, or {@code "https"}, if no scheme
   *         is present in the spec.
   */
  private static String getScheme(@NonNull Swagger swagger) {
    List<Scheme> schemes = swagger.getSchemes();
    if (schemes != null && !schemes.isEmpty()) {
      return schemes.get(0).toValue();
    }
    return "https";
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
    return Optional.ofNullable(containerRequest.getRequestHeaders().getFirst("x-forwarded-host"))
        .map(header -> header.split(",")[0]);
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
