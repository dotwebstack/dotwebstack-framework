package org.dotwebstack.framework.core.config.validators;

import static org.springframework.util.StringUtils.uncapitalize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;

@Slf4j
public final class ValidSortAndFilterFields {

  private static final int MAX_DEPTH = 10;

  private ValidSortAndFilterFields() {}

  public static List<String> get(DotWebStackConfiguration dotWebStackConfiguration) {
    return get(dotWebStackConfiguration, 0);
  }

  public static List<String> get(DotWebStackConfiguration dotWebStackConfiguration, int initialDepth) {
    Map<String, AbstractTypeConfiguration<?>> objectTypes = dotWebStackConfiguration.getObjectTypes();

    return objectTypes.entrySet()
        .stream()
        .map(entry -> getValidSortAndFilterFields(objectTypes, uncapitalize(entry.getKey()), entry.getValue(),
            initialDepth))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterFields(Map<String, AbstractTypeConfiguration<?>> objectTypes,
      String parentFieldPath, AbstractTypeConfiguration<?> typeConfiguration, int depth) {
    Map<String, ? extends AbstractFieldConfiguration> fields = typeConfiguration.getFields();

    return fields.values()
        .stream()
        .filter(field -> !field.isList())
        .filter(field -> !field.isAggregateField())
        .map(field -> getValidSortAndFilterField(objectTypes, parentFieldPath, field, depth))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterField(Map<String, AbstractTypeConfiguration<?>> objectTypes,
      String parentFieldPath, AbstractFieldConfiguration field, int depth) {
    String currentFieldPath = parentFieldPath.concat(".")
        .concat(field.getName());

    if (depth > MAX_DEPTH) {
      return List.of();
    }

    if ((field.isNestedObjectField() || field.isObjectField())) {
      return getValidSortAndFilterFields(objectTypes, currentFieldPath, objectTypes.get(field.getType()), depth + 1);
    }

    if (field.isScalarField()) {
      return List.of(currentFieldPath);
    }

    return List.of();
  }
}
