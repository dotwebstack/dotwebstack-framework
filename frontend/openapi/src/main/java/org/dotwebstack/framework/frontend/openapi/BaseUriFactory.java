package org.dotwebstack.framework.frontend.openapi;

import java.net.URI;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UtilityClass
public class BaseUriFactory {

  private static final Logger LOG = LoggerFactory.getLogger(BaseUriFactory.class);

  public static String newBaseUri(@NonNull URI absolutePath, @NonNull String basePath) {
    String baseUri = "";
    try {
      URI base = new URIBuilder()
          .setScheme(absolutePath.getScheme())
          .setHost(absolutePath.getHost())
          .setPort(absolutePath.getPort())
          .setPath(basePath)
          .build();
      baseUri = base.toString();
      LOG.info(baseUri);
    } catch (Exception exc) {
      LOG.error("BaseUri could not be made");
    }
    return baseUri;
  }
}
