package org.dotwebstack.framework.backend.postgres;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.util.Objects;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;

public class TestHelper {

  private final BackendModule<?> backendModule;

  public TestHelper(BackendModule<?> backendModule) {
    this.backendModule = backendModule;
  }

  public void initObjecTypes(String pathToConfigFile) {
    Schema schema = getSchema(pathToConfigFile);
    backendModule.init(schema.getObjectTypes());
  }

  public Schema getSchema(String pathToConfigFile) {
    var objectMapper = createObjectMapper();

    return new SchemaReader(objectMapper).read(pathToConfigFile);
  }

  private ObjectMapper createObjectMapper() {
    var deserializerModule =
        new SimpleModule().addDeserializer(ObjectType.class, new TestHelper.ObjectTypeDeserializer(backendModule));

    return YAMLMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .addModule(deserializerModule)
        .build();
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
