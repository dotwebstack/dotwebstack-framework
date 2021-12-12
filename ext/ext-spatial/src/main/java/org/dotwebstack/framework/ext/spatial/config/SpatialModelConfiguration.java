package org.dotwebstack.framework.ext.spatial.config;

import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.ext.spatial.backend.SpatialBackendModule;
import org.dotwebstack.framework.ext.spatial.model.Schema;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SpatialModelConfiguration {

  private final SpatialBackendModule<?> backendModule;

  public SpatialModelConfiguration(SpatialBackendModule<?> backendModule) {
    this.backendModule = backendModule;
  }

  @Bean
  Spatial spatial(@Value("${dotwebstack.config:dotwebstack.yaml}") String configFile) {
    var objectMapper = createObjectMapper();

    Schema schema = new SpatialModelReader(objectMapper).read(configFile);

    if (schema.getSpatial() == null) {
      LOG.warn("No spatial configuration is found in '{}'.", configFile);
      return new Spatial();
    }

    validateSpatialFields(configFile, schema);

    backendModule.init(schema.getSpatial());

    return schema.getSpatial();
  }

  private ObjectMapper createObjectMapper() {
    var deserializerModule = new SimpleModule().addDeserializer(SpatialReferenceSystem.class,
        new SpatialReferenceSystemDeserializer(backendModule));

    return YAMLMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .addModule(deserializerModule)
        .build();
  }

  private void validateSpatialFields(String configFile, Schema schema) {
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

  private static class SpatialReferenceSystemDeserializer extends JsonDeserializer<SpatialReferenceSystem> {

    private final SpatialBackendModule<?> backendModule;

    public SpatialReferenceSystemDeserializer(SpatialBackendModule<?> backendModule) {
      this.backendModule = backendModule;
    }

    @Override
    public SpatialReferenceSystem deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      return parser.readValueAs(backendModule.getSpatialReferenceSystemClass());
    }
  }
}
