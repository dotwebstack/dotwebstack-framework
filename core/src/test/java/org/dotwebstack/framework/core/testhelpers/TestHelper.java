package org.dotwebstack.framework.core.testhelpers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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

  public static ObjectMapper createSimpleObjectMapper() {
    var deserializerModule =
        new SimpleModule().addDeserializer(ObjectType.class, new JsonDeserializer<TestObjectType>() {
          @Override
          public TestObjectType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
              throws IOException {
            var objectType = jsonParser.readValueAs(TestObjectType.class);

            objectType.setName(jsonParser.getCurrentName());
            objectType.getFields()
                .forEach((name, field) -> field.setName(name));

            return objectType;
          }
        });

    return new ObjectMapper(new YAMLFactory()).enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(deserializerModule);
  }

  public Schema loadSchema(String pathToConfigFile) {
    var objectMapper = createObjectMapper();
    var schema = new SchemaReader(objectMapper).read(pathToConfigFile);

    backendModule.init(schema.getObjectTypes());

    return schema;
  }

  private ObjectMapper createObjectMapper() {
    var deserializerModule =
        new SimpleModule().addDeserializer(ObjectType.class, new TestHelper.ObjectTypeDeserializer(backendModule));

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
