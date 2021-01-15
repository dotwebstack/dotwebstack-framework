package org.dotwebstack.framework.backend.json;

import static org.dotwebstack.framework.backend.json.JsonResourceLoader.JSON_DATA_PATH;
import static org.dotwebstack.framework.backend.json.JsonResourceLoader.loadJsonResource;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonDataService {

  private final ObjectMapper jsonMapper = new ObjectMapper();

  private final ConcurrentHashMap<String, JsonNode> jsonSourceCache = new ConcurrentHashMap<>();

  private final ResourceLoader resourceLoader;

  public JsonDataService(@NonNull ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  @PostConstruct
  public void loadJsonData() throws IOException {
    Optional<Resource> jsonDataResource = ResourceLoaderUtils.getResource(JSON_DATA_PATH);
    if (jsonDataResource.isPresent()) {
      Resource[] resourceList = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
          .getResources(jsonDataResource.get()
              .getURI() + "**.json");

      Stream.of(resourceList)
          .filter(Resource::exists)
          .peek(location -> LOG.debug("Looking for JSON files in {}", location))
          .map(Resource::getFilename)
          .forEach(this::readJsonDataFile);
    }
  }

  public JsonNode getJsonSourceData(String fileName) {
    JsonNode jsonData = jsonSourceCache.get(fileName);

    return jsonData != null ? jsonData : readJsonDataFile(fileName);
  }

  private JsonNode readJsonDataFile(String fileName) {
    Optional<Resource> data = loadJsonResource(fileName);

    try (InputStream jsonDataInputStream = data.get()
        .getInputStream()) {
      JsonNode jsonDataAsNode = jsonMapper.readTree(jsonDataInputStream);
      jsonSourceCache.put(fileName, jsonDataAsNode);

      return jsonDataAsNode;

    } catch (IOException exception) {
      throw illegalStateException(
          String.format("Encountered IOException while reading: %s.", JSON_DATA_PATH + fileName));
    }
  }
}
