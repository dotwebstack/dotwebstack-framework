package org.dotwebstack.framework.core.helpers;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.dotwebstack.framework.core.ResourceProperties;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResourceLoaderUtils {

  @SneakyThrows
  public static URI getResourceLocation(@NonNull String resourceLocation) {
    URI resourceAsUri = null;

    URI uri = ResourceProperties.getFileConfigPath()
        .resolve(resourceLocation);
    if (uriExists(uri)) {
      resourceAsUri = uri;
    } else {
      URL classpathUrl = ResourceLoaderUtils.class.getResource(ResourceProperties.getResourcePath()
          .resolve(resourceLocation)
          .getPath());
      if (classpathUrl != null) {
          uri = classpathUrl.toURI();

        if (uriExists(uri)) {
          resourceAsUri = uri;
        }
      }
    }
    return resourceAsUri;
  }

  static boolean uriExists(URI uri) {
    return Files.exists(Paths.get(uri));
  }
}
