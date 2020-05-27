package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.dotwebstack.framework.core.CoreProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(CoreProperties.class)
public class ResourceLoaderUtils {

  private CoreProperties coreProperties;

  public ResourceLoaderUtils(CoreProperties coreProperties) {
    this.coreProperties = coreProperties;
  }

  public URI getResourceLocation(String resourceLocation) {
    URI resourceAsUri = null;

    URI uri = coreProperties.getFileConfigPath()
        .resolve(resourceLocation);
    if (Files.exists(Paths.get(uri))) {
      resourceAsUri = uri;
    } else {
      URL classpathUrl = getClass().getResource(coreProperties.getResourcePath()
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
