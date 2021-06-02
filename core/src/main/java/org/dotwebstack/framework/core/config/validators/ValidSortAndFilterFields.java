package org.dotwebstack.framework.core.config.validators;

import static org.springframework.util.StringUtils.uncapitalize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;

public final class ValidSortAndFilterFields {

  private ValidSortAndFilterFields() {}

  public static List<String> get(DotWebStackConfiguration dotWebStackConfiguration) {
    Map<String, AbstractTypeConfiguration<?>> objectTypes = dotWebStackConfiguration.getObjectTypes();

    return objectTypes.entrySet()
        .stream()
        .map(entry -> getValidSortAndFilterFields(objectTypes, uncapitalize(entry.getKey()), entry.getValue()))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterFields(Map<String, AbstractTypeConfiguration<?>> objectTypes,
      String parentFieldPath, AbstractTypeConfiguration<?> typeConfiguration) {
    Map<String, ? extends AbstractFieldConfiguration> fields = typeConfiguration.getFields();

    return fields.values()
        .stream()
        .filter(field -> !field.isList())
        .filter(field -> !field.isAggregateField())
        .map(field -> getValidSortAndFilterField(objectTypes, parentFieldPath, field))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterField(Map<String, AbstractTypeConfiguration<?>> objectTypes,
      String parentFieldPath, AbstractFieldConfiguration field) {
    String currentFieldPath = parentFieldPath.concat(".")
        .concat(field.getName());

    if (field.isNestedObjectField() || field.isObjectField()) {
      return getValidSortAndFilterFields(objectTypes, currentFieldPath, objectTypes.get(field.getType()));
    } else {
      return List.of(currentFieldPath);
    }
  }
}
