package org.dotwebstack.framework.core.config.validators;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Conditional(GraphQlNativeEnabled.class)
@Component
public class SortValidator implements DotWebStackConfigurationValidator {

  @Override
  public void validate(DotWebStackConfiguration dotWebStackConfiguration) {
    Map<String, List<SortableByConfiguration>> sortableByPerObjectTypeName =
        getSortableByPerObjectTypeName(dotWebStackConfiguration);

    sortableByPerObjectTypeName.forEach((objectType, sortableByList) -> sortableByList.forEach(
        sortableByConfiguration -> validateSortableByField(dotWebStackConfiguration.getTypeConfiguration(objectType),
            sortableByConfiguration)));
  }

  private Map<String, List<SortableByConfiguration>> getSortableByPerObjectTypeName(
      DotWebStackConfiguration dotWebStackConfiguration) {
    return dotWebStackConfiguration.getObjectTypes()
        .entrySet()
        .stream()
        .filter(this::isSortableByListNotEmpty)
        .collect(Collectors.toMap(Map.Entry::getKey, this::getSortableByConfiguration));
  }

  private boolean isSortableByListNotEmpty(Map.Entry<String, AbstractTypeConfiguration<?>> entry) {
    return !entry.getValue()
        .getSortableBy()
        .isEmpty();
  }

  private List<SortableByConfiguration> getSortableByConfiguration(
      Map.Entry<String, AbstractTypeConfiguration<?>> entry) {
    return entry.getValue()
        .getSortableBy()
        .values()
        .stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private void validateSortableByField(AbstractTypeConfiguration<?> objectType,
      SortableByConfiguration sortableByConfiguration) {
    String fieldName = sortableByConfiguration.getField();

    if (!objectType.getField(fieldName)
        .isPresent()) {
      throw new InvalidConfigurationException(
          String.format("Sort field '%s' in object type '%s' can't be resolved to a single scalar type.", fieldName,
              objectType.getName()));
    }
  }
}
