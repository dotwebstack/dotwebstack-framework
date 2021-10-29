package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.core.io.Resource;

public class SchemaReader {

  private final ObjectMapper objectMapper;

  public SchemaReader(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Schema read(String configFile) {
    return ResourceLoaderUtils.getResource(configFile)
        .map(this::read)
        .orElseThrow(() -> invalidConfigurationException("Config file not found on location: {}", configFile));
  }

  private Schema read(Resource resource) {
    try {
      return objectMapper.readValue(resource.getInputStream(), Schema.class);
    } catch (IOException e) {
      throw invalidConfigurationException("Error while reading config file.", e);
    }
  }
}
