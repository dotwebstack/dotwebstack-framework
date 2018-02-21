package org.dotwebstack.framework.frontend.openapi;

import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.http.client.utils.URIBuilder;

@UtilityClass
public class BaseUriFactory {

  public static String newBaseUri(@NonNull Swagger swagger) {
    String baseUri;
    try {
      String scheme = "https";
      List<Scheme> schemes = swagger.getSchemes();
      if (schemes != null && !schemes.isEmpty()) {
        scheme = schemes.get(0).toValue();
      }
      URI base = new URIBuilder()
          .setScheme(scheme)
          .setHost(swagger.getHost())
          .setPath(swagger.getBasePath())
          .build();
      baseUri = base.toString();
    } catch (URISyntaxException use) {
      throw new IllegalStateException("BaseUri could not be made", use);
    }
    return baseUri;
  }
}
