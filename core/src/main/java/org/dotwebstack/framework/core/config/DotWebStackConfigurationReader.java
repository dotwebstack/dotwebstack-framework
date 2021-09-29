package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.backend.BackendDefinition;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

public class DotWebStackConfigurationReader {

  private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

  public DotWebStackConfigurationReader(BackendDefinition backendDefinition) {
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new SimpleModule()
            .addDeserializer(AbstractTypeConfiguration.class, new TypeConfigurationDeserializer(backendDefinition)));
  }

  public DotWebStackConfiguration<?> read(String filename) {
    return ResourceLoaderUtils.getResource(filename)
        .map(resource -> {
          try {
            return objectMapper.readValue(resource.getInputStream(), DotWebStackConfiguration.class);
          } catch (IOException e) {
            throw new InvalidConfigurationException("Error while reading config file.", e);
          }
        })
        .map(configuration -> {
          Set<ConstraintViolation<DotWebStackConfiguration<?>>> violations = Validation.buildDefaultValidatorFactory()
              .getValidator()
              .validate(configuration);

          if (!violations.isEmpty()) {
            throw invalidConfigurationException("Config file contains validation errors: {}", violations);
          }

          return configuration;
        })
        .orElseThrow(() -> invalidConfigurationException("Config file not found on location: {}", filename));
  }

  private static class TypeConfigurationDeserializer extends JsonDeserializer<AbstractTypeConfiguration<?>> {

    private final BackendDefinition backendDefinition;

    public TypeConfigurationDeserializer(BackendDefinition backendDefinition) {
      this.backendDefinition = backendDefinition;
    }

    @Override
    public AbstractTypeConfiguration<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
      return parser.readValueAs(backendDefinition.getTypeConfigurationClass());
    }
  }
}
