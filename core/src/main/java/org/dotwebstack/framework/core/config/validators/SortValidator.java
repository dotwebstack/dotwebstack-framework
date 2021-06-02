package org.dotwebstack.framework.core.config.validators;

import static org.springframework.util.StringUtils.uncapitalize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
import org.springframework.stereotype.Component;

@Component
public class SortValidator implements DotWebStackConfigurationValidator {

  @Override
  public void validate(DotWebStackConfiguration dotWebStackConfiguration) {
    List<String> validSortFields = ValidSortAndFilterFields.get(dotWebStackConfiguration);

    Map<String, List<SortableByConfiguration>> sortableByPerObjectTypeName =
        getSortableByPerObjectTypeName(dotWebStackConfiguration);

    sortableByPerObjectTypeName.forEach((objectTypeName, sortableByList) -> sortableByList.forEach(
        sortableByConfiguration -> validateSortableByField(validSortFields, objectTypeName, sortableByConfiguration)));
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

  private void validateSortableByField(List<String> validSortFields, String objectTypeName,
      SortableByConfiguration sortableByConfiguration) {
    String sortableByFieldName = getSortableByFieldName(objectTypeName, sortableByConfiguration);

    if (!validSortFields.contains(sortableByFieldName)) {
      throw new InvalidConfigurationException(String
          .format("SortableBy field '%s' in object type '%s' can't be resolved.", sortableByFieldName, objectTypeName));
    }
  }

  private String getSortableByFieldName(String objectTypeName, SortableByConfiguration sortableByConfiguration) {
    String fieldName = sortableByConfiguration.getField();

    return fieldName.contains(".") ? fieldName
        : uncapitalize(objectTypeName).concat(".")
            .concat(fieldName);
  }
}
