package org.dotwebstack.framework.core.config;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.dotwebstack.framework.core.datafetchers.FieldKeyCondition;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;


@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "backend")
public abstract class AbstractTypeConfiguration<T extends AbstractFieldConfiguration> implements TypeConfiguration<T> {

  protected String name;

  @NotNull
  @Valid
  @Size(min = 1, max = 1)
  protected List<KeyConfiguration> keys;

  @NotNull
  @Valid
  protected Map<String, T> fields = new HashMap<>();

  public void init(ObjectTypeDefinition objectTypeDefinition) {}

  public KeyCondition getKeyCondition(String fieldName) {
    if (!fields.containsKey(fieldName)) {
      throw invalidConfigurationException("Field with name {} not found in configuration", fieldName);
    }

    AbstractFieldConfiguration fieldConfiguration = fields.get(fieldName);

    if (fieldConfiguration.getMappedBy() != null) {
      return MappedByKeyCondition.builder()
          .fieldName(fieldConfiguration.getMappedBy())
          .build();
    }

    throw unsupportedOperationException("Unable to create keyCondition for fieldName {}", fieldName);
  }

  protected Optional<FieldKeyCondition> getQueryArgumentKeyConditions(DataFetchingEnvironment environment,
      boolean existingField) {
    Map<String, Object> entries = environment.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> !existingField || fields.containsKey(argument.getName()))
        .map(argument -> getQueryArgumentEntry(environment, argument))
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    if (entries.size() > 0) {
      return Optional.of(FieldKeyCondition.builder()
          .fieldValues(entries)
          .build());
    }

    return Optional.empty();
  }

  public Map.Entry<String, Object> getQueryArgumentEntry(DataFetchingEnvironment environment,
      GraphQLArgument argument) {
    Object value = environment.getArguments()
        .get(argument.getName());

    if (value == null) {
      return null;
    }

    return Map.entry(argument.getName(), value);
  }
}
