package org.dotwebstack.framework.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import org.dotwebstack.framework.core.model.ObjectType;

public class TestHelper {

  public static ObjectMapper createObjectMapper() {
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
}
