package org.dotwebstack.framework.core.config.validators;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(GraphQlNativeEnabled.class)
@Component
public class SortValidator implements SchemaValidator {

  @Override
  public void validate(Schema schema) {
    Map<String, List<SortableByConfiguration>> sortableByPerObjectTypeName = getSortableByPerObjectTypeName(schema);

    sortableByPerObjectTypeName.forEach((objectType, sortableByList) -> sortableByList
        .forEach(sortableByConfiguration -> validateSortableByField(schema.getObjectType(objectType)
            .orElseThrow(), sortableByConfiguration)));
  }

  private Map<String, List<SortableByConfiguration>> getSortableByPerObjectTypeName(Schema schema) {
    return schema.getObjectTypes()
        .entrySet()
        .stream()
        .filter(this::isSortableByListNotEmpty)
        .collect(Collectors.toMap(Map.Entry::getKey, this::getSortableByConfiguration));
  }

  private boolean isSortableByListNotEmpty(Map.Entry<String, ? extends ObjectType<?>> entry) {
    return !entry.getValue()
        .getSortableBy()
        .isEmpty();
  }

  private List<SortableByConfiguration> getSortableByConfiguration(Map.Entry<String, ? extends ObjectType<?>> entry) {
    return entry.getValue()
        .getSortableBy()
        .values()
        .stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private void validateSortableByField(ObjectType<?> objectType, SortableByConfiguration sortableByConfiguration) {
    String fieldName = sortableByConfiguration.getField();

    if (objectType.getField(fieldName)
        .isEmpty()) {
      throw invalidConfigurationException(
          "Sort field '{}' in object type '{}' can't be resolved to a single scalar type.", fieldName,
          objectType.getName());
    }
  }
}
