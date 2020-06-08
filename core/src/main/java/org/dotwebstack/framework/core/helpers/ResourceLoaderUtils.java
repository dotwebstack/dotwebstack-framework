package org.dotwebstack.framework.core.helpers;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.dotwebstack.framework.core.ResourceProperties;

public class ResourceLoaderUtils {

  private ResourceLoaderUtils() {}

  @SneakyThrows
  public static Optional<URI> getResourceLocation(@NonNull String resourceLocation) {
    URI uri = resolve(ResourceProperties.getFileConfigPath(), resourceLocation);
    if (uriExists(uri)) {
      return Optional.of(uri);
    } else {
      uri = resolve(ResourceProperties.getResourcePath(), resourceLocation);
      URL classpathUrl = ResourceLoaderUtils.class.getResource(uri.getPath());
      if (classpathUrl != null) {
        uri = classpathUrl.toURI();

        if (uriExists(uri)) {
          return Optional.of(uri);
        }
      }
    }
    return Optional.empty();
  }

  private static URI resolve(@NonNull URI basePath, String resourceLocation) {
    if (resourceLocation == null) {
      return basePath;
    }
    return basePath.resolve(resourceLocation);
  }

  static boolean uriExists(URI uri) {
    return Files.exists(Paths.get(uri));
  }
}
