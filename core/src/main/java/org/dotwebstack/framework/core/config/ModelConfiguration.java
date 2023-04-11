package org.dotwebstack.framework.core.config;

import static java.util.stream.Collectors.joining;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.backend.BackendModule;
import org.dotwebstack.framework.core.config.validators.SchemaValidator;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(OnLocalSchema.class)
public class ModelConfiguration {

  private final BackendModule<?> backendModule;

  public ModelConfiguration(BackendModule<?> backendModule) {
    this.backendModule = backendModule;
  }

  @Bean
  Schema schema(@Value("${dotwebstack.config:dotwebstack.yaml}") String configFile, List<SchemaValidator> validators) {
    var objectMapper = createObjectMapper();

    Schema schema = new SchemaReader(objectMapper).read(configFile);
    validateSchemaFields(configFile, schema);

    addImplementedFields(schema);

    var schemaTypes = schema.getObjectTypes();
    schemaTypes.putAll(schema.getInterfaces());

    backendModule.init(schemaTypes);

    validators.forEach(validator -> validator.validate(schema));

    return schema;
  }

  private void addImplementedFields(Schema schema) {
    schema.getInterfaces()
        .forEach((interfaceName, interfaceType) -> interfaceType.getImplements()
            .forEach(implementz -> {
              if (schema.getInterfaces()
                  .containsKey(implementz)) {
                schema.getInterfaces()
                    .get(implementz)
                    .getFields()
                    .forEach(interfaceType::addField);
              } else {
                throw invalidConfigurationException(
                    "Implemented Interface '{}' not found in provided schema for Interface '{}'.", implementz,
                    interfaceName);
              }
            }));

    schema.getObjectTypes()
        .forEach((objectName, objectType) -> objectType.getImplements()
            .forEach(implementz -> {
              if (schema.getInterfaces()
                  .containsKey(implementz)) {
                schema.getInterfaces()
                    .get(implementz)
                    .getFields()
                    .forEach(objectType::addField);
              } else {
                throw invalidConfigurationException(
                    "Implemented Interface '{}' not found in provided schema for ObjectType '{}'.", implementz,
                    objectName);
              }
            }));
  }

  private ObjectMapper createObjectMapper() {
    var deserializerModule =
        new SimpleModule().addDeserializer(ObjectType.class, new ObjectTypeDeserializer(backendModule));

    return YAMLMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .addModule(deserializerModule)
        .build();
  }

  private void validateSchemaFields(String configFile, Schema schema) {
    Set<ConstraintViolation<Object>> violations = Validation.buildDefaultValidatorFactory()
        .getValidator()
        .validate(schema);

    if (!violations.isEmpty()) {
      String msg = String.format("%s is not valid. Reasons (%s):%n", configFile, violations.size());
      String violationLines = violations.stream()
          .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
          .collect(joining(", " + System.lineSeparator()));
      throw new ConstraintViolationException(msg + violationLines, violations);
    }
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
            .forEach(this::setFilterProperties);
      }

      return objectType;
    }

    private void setFilterProperties(Map.Entry<String, FilterConfiguration> filterEntry) {
      var filter = filterEntry.getValue();
      var filterName = filterEntry.getKey();

      filter.setName(filterName);

      if (Objects.isNull(filter.getField())) {
        filter.setField(filterName);
      }
    }
  }
}
