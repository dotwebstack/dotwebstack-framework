package org.dotwebstack.framework.backend.json.config;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("json")
public class JsonTypeConfiguration extends AbstractTypeConfiguration<JsonFieldConfiguration> {

  @Valid
  private Map<String, String> queryPaths;

  @Valid
  @NotBlank
  private String file;

  @Override
  public void init(Map<String, AbstractTypeConfiguration<?>> objectTypes, ObjectTypeDefinition objectTypeDefinition) {
    fields.entrySet()
        .stream()
        .filter(entry -> Objects.nonNull(entry.getValue()))
        .filter(entry -> isNotEmpty(entry.getValue()
            .getAggregationOf()))
        .findFirst()
        .ifPresent(entry -> {
          throw invalidConfigurationException(
              "Usage of 'aggregationOf' by field '{}.{}' is not supported with an JSON backend",
              objectTypeDefinition.getName(), entry.getKey());
        });
  }

  public String getJsonPathTemplate(String queryName) {
    if (queryPaths.containsKey(queryName)) {
      return queryPaths.get(queryName);
    }

    throw illegalArgumentException("No path configured for query with name '{}'", queryName);
  }

  public String getDataSourceFile() {
    return file;
  }

  @Override
  public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
    return getQueryArgumentKeyConditions(environment, false).orElse(null);
  }

  @Override
  public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
    return null;
  }

  @Override
  public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
    return null;
  }
}
