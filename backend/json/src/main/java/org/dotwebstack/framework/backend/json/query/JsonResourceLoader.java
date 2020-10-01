package org.dotwebstack.framework.backend.json.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.Optional;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.core.io.Resource;

public class JsonResourceLoader {

  public static final String JSON_DATA_PATH = "data/";

  public static Optional<Resource> loadJsonResource(String fileName) {
    Optional<Resource> data = ResourceLoaderUtils.getResource(JSON_DATA_PATH + fileName);

    if (data.isEmpty()) {
      throw invalidConfigurationException(
          String.format("JSON data file: %s does not exists", JSON_DATA_PATH + fileName));
    }

    return data;

  }
}
