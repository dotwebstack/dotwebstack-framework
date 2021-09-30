package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
class ModelConfiguration {

  private final BackendModule<?> backendModule;

  ModelConfiguration(BackendModule<?> backendModule) {
    this.backendModule = backendModule;
  }

  @Bean
  Schema schema(@Value("${dotwebstack.config:dotwebstack.yaml}") String configFile) {
    return ResourceLoaderUtils.getResource(configFile)
        .map(this::parseResource)
        .orElseThrow(() -> invalidConfigurationException("Config file not found on location: {}", configFile));
  }

  private Schema parseResource(Resource resource) {
    try {
      return createObjectMapper().readValue(resource.getInputStream(), Schema.class);
    } catch (IOException e) {
      throw invalidConfigurationException("Error while reading config file.", e);
    }
  }

  private ObjectMapper createObjectMapper() {
    var deserializerModule =
        new SimpleModule().addDeserializer(ObjectType.class, new ObjectTypeDeserializer(backendModule));

    return new ObjectMapper(new YAMLFactory()).enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(deserializerModule);
  }

  private static class ObjectTypeDeserializer extends JsonDeserializer<ObjectType<? extends ObjectField>> {

    private final BackendModule<?> backendModule;

    public ObjectTypeDeserializer(BackendModule<?> backendModule) {
      this.backendModule = backendModule;
    }

    @Override
    public ObjectType<? extends ObjectField> deserialize(JsonParser parser, DeserializationContext context)
        throws IOException {
      return parser.readValueAs(backendModule.getObjectTypeClass());
    }
  }
}
