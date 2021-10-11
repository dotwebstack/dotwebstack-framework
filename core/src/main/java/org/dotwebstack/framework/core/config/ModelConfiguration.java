package org.dotwebstack.framework.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.config.validators.SchemaValidator;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfiguration {

  private final BackendModule<?> backendModule;

  public ModelConfiguration(BackendModule<?> backendModule) {
    this.backendModule = backendModule;
  }

  @Bean
  Schema schema(@Value("${dotwebstack.config:dotwebstack.yaml}") String configFile, List<SchemaValidator> validators) {
    var objectMapper = createObjectMapper();

    Schema schema = new SchemaReader(objectMapper).read(configFile);
    validators.forEach(validator -> validator.validate(schema));
    return schema;
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
      var objectType = parser.readValueAs(backendModule.getObjectTypeClass());

      objectType.setName(parser.getCurrentName());
      objectType.getFields()
          .forEach((name, field) -> {
            field.setObjectType(objectType);
            field.setName(name);
          });

      if (objectType.getFilters() != null) {
        objectType.getFilters()
            .entrySet()
            .stream()
            .filter(entry -> Objects.isNull(entry.getValue()
                .getField()))
            .forEach(entry -> entry.getValue()
                .setField(entry.getKey()));
      }

      return objectType;
    }
  }
}
