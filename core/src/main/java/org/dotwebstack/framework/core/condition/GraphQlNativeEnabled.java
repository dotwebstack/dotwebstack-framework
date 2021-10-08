package org.dotwebstack.framework.core.condition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


public class GraphQlNativeEnabled implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    String configFilename = context.getEnvironment()
        .getProperty("dotwebstack.config");
    if (configFilename == null) {
      configFilename = "dotwebstack.yaml";
    }

    var schema = readSchema(configFilename);

    // if no proxy is configured, use the local/native graphql service
    return schema.getSettings() == null || schema.getSettings()
        .getGraphql() == null || schema.getSettings()
            .getGraphql()
            .getProxy() == null;
  }

  protected Schema readSchema(String configFilename) {
    var objectMapper = createObjectMapper();

    return new SchemaReader(objectMapper).read(configFilename);
  }

  private ObjectMapper createObjectMapper() {
    var deserializerModule = new SimpleModule().addDeserializer(ObjectType.class, new JsonDeserializer<>() {
      @Override
      public ObjectType<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        return null;
      }
    });

    return new ObjectMapper(new YAMLFactory()).enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(deserializerModule);
  }

}
