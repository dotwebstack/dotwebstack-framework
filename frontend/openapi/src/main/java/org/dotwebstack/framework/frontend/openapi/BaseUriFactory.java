package org.dotwebstack.framework.frontend.openapi;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.http.client.utils.URIBuilder;

@UtilityClass
public class BaseUriFactory {

  public static String newBaseUri(@NonNull URI absolutePath, @NonNull String basePath) {
    String baseUri;
    try {
      URI base = new URIBuilder()
          .setScheme(absolutePath.getScheme())
          .setHost(absolutePath.getHost())
          .setPort(absolutePath.getPort())
          .setPath(basePath)
          .build();
      baseUri = base.toString();
    } catch (URISyntaxException use) {
      throw new IllegalStateException("BaseUri could not be made", use);
    }
    return baseUri;
  }
}
