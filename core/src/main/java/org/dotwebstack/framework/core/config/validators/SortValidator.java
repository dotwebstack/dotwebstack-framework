package org.dotwebstack.framework.core.config.validators;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class SortValidator implements SchemaValidator {

  @Override
  public void validate(Schema schema) {
    Map<String, List<SortableByConfiguration>> sortableByPerObjectTypeName = getSortableByPerObjectTypeName(schema);

    sortableByPerObjectTypeName.forEach((objectTypeName, sortableByList) -> sortableByList
        .forEach(sortableByConfiguration -> validateSortableByField(schema, objectTypeName, sortableByConfiguration)));
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
        .toList();
  }

  private void validateSortableByField(Schema schema, String objectTypeName,
      SortableByConfiguration sortableByConfiguration) {
    String sortFieldPath = sortableByConfiguration.getField();
    String[] sortFieldPathArr = sortFieldPath.split("\\.");

    Optional<? extends ObjectField> field = getField(schema, objectTypeName, sortFieldPathArr);

    if (field.isEmpty()) {
      throw invalidConfigurationException(
          "Sort field '{}' in object type '{}' can't be resolved to a single scalar type.", sortFieldPath,
          objectTypeName);
    }
  }
}
