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
import java.util.Objects;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.helpers.ResourceLoaderUtils;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

public class DotWebStackConfigurationReader {
  private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

  public DotWebStackConfigurationReader() {
    this(null);
  }

  public DotWebStackConfigurationReader(Class<? extends AbstractTypeConfiguration<?>> typeConfigurationClass) {
    if (typeConfigurationClass == null) {
      typeConfigurationClass = findTypeConfigurationClass();
    }

    var deserializerModule = new SimpleModule().addDeserializer(AbstractTypeConfiguration.class,
        new AbstractTypeConfigurationDeserializer(typeConfigurationClass));

    objectMapper.registerModule(deserializerModule);
    objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public DotWebStackConfiguration read(String filename) {
    return ResourceLoaderUtils.getResource(filename)
        .map(resource -> {
          try {
            return objectMapper.readValue(resource.getInputStream(), DotWebStackConfiguration.class);
          } catch (IOException e) {
            throw new InvalidConfigurationException("Error while reading config file.", e);
          }
        })
        .map(configuration -> {
          Set<ConstraintViolation<DotWebStackConfiguration>> violations = Validation.buildDefaultValidatorFactory()
              .getValidator()
              .validate(configuration);

          if (!violations.isEmpty()) {
            throw invalidConfigurationException("Config file contains validation errors: {}", violations);
          }

          return configuration;
        })
        .orElseThrow(() -> invalidConfigurationException("Config file not found on location: {}", filename));
  }

  @SuppressWarnings("unchecked")
  private Class<? extends AbstractTypeConfiguration<?>> findTypeConfigurationClass() {
    var scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AssignableTypeFilter(TypeConfiguration.class));

    return scanner.findCandidateComponents("org.dotwebstack.framework.backend")
        .stream()
        .map(beanDefinition -> ClassUtils.resolveClassName(Objects.requireNonNull(beanDefinition.getBeanClassName()),
            getClass().getClassLoader()))
        .findFirst()
        .map(c -> (Class<? extends AbstractTypeConfiguration<?>>) c)
        .orElseThrow(() -> invalidConfigurationException("No implementation found for AbstractTypeConfiguration."));
  }

  private static class AbstractTypeConfigurationDeserializer extends JsonDeserializer<AbstractTypeConfiguration<?>> {

    private final Class<? extends AbstractTypeConfiguration<?>> typeConfigurationClass;

    public AbstractTypeConfigurationDeserializer(Class<? extends AbstractTypeConfiguration<?>> typeConfigurationClass) {
      this.typeConfigurationClass = typeConfigurationClass;
    }

    @Override
    public AbstractTypeConfiguration<?> deserialize(JsonParser parser, DeserializationContext context)
        throws IOException {
      return parser.readValueAs(typeConfigurationClass);
    }
  }
}
