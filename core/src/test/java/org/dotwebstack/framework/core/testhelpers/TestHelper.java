package org.dotwebstack.framework.core.testhelpers;

import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.dotwebstack.framework.core.CoreConfigurer;
import org.dotwebstack.framework.core.TypeDefinitionRegistrySchemaFactory;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.config.ModelConfiguration;
import org.dotwebstack.framework.core.config.SchemaReader;
import org.dotwebstack.framework.core.datafetchers.filter.CoreFilterConfigurer;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.r2dbc.core.DatabaseClient;

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

    return YAMLMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .addModule(deserializerModule)
        .build();
  }

  public Schema loadSchema(String pathToConfigFile) {
    var objectMapper = createObjectMapper();
    var schema = new SchemaReader(objectMapper).read(pathToConfigFile);

    backendModule.init(schema.getObjectTypes());

    return schema;
  }

  public static Schema loadSchemaWithDefaultBackendModule(String pathToConfigFile) {
    var backendModule = new TestBackendModule(new TestBackendLoaderFactory(mock(DatabaseClient.class)));
    var modelConfig = new ModelConfiguration(backendModule);
    return modelConfig.schema(pathToConfigFile, List.of());
  }

  public static GraphQLSchema schemaToGraphQl(Schema schema) {
    var typeDefinitionRegistry = new TypeDefinitionRegistrySchemaFactory(schema, List.of(new CoreFilterConfigurer()))
        .createTypeDefinitionRegistry();

    var runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();

    var coreConfigurer = new CoreConfigurer();
    coreConfigurer.configureTypeDefinitionRegistry(typeDefinitionRegistry);
    coreConfigurer.configureRuntimeWiring(runtimeWiringBuilder);

    return schemaToGraphQl(typeDefinitionRegistry, runtimeWiringBuilder.build());
  }

  public static GraphQLSchema schemaToGraphQl(TypeDefinitionRegistry typeDefinitionRegistry, RuntimeWiring runtimeWiring) {


    return new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
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
