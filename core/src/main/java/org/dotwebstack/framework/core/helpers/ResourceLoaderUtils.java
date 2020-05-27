package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.dotwebstack.framework.core.ResourceProperties;

public class ResourceLoaderUtils {

  private ResourceLoaderUtils() {}

  public static URI getResourceLocation(String resourceLocation) {
    URI resourceAsUri = null;

    URI uri = ResourceProperties.getFileConfigPath()
        .resolve(resourceLocation);
    if (Files.exists(Paths.get(uri))) {
      resourceAsUri = uri;
    } else {
      URL classpathUrl = ResourceLoaderUtils.class.getResource(ResourceProperties.getResourcePath()
          .resolve(resourceLocation)
          .getPath());
      if (classpathUrl != null) {
        try {
          uri = classpathUrl.toURI();
        } catch (Exception e) {
          throw illegalStateException("Cannot get URI from classpathUrl");
        }

        if (Files.exists(Paths.get(uri))) {
          resourceAsUri = uri;
        }
      }
    }
    return resourceAsUri;
  }
}
