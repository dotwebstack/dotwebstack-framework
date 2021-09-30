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

  private static Map<String, AbstractTypeConfiguration<?>> OBJECT_TYPES;

  private ValidSortAndFilterFields() {}

  public static List<String> get(DotWebStackConfiguration dotWebStackConfiguration) {
    return get(dotWebStackConfiguration, 0);
  }

  public static List<String> get(DotWebStackConfiguration dotWebStackConfiguration, int initialDepth) {
    OBJECT_TYPES = dotWebStackConfiguration.getObjectTypes();

    return OBJECT_TYPES.entrySet()
        .stream()
        .map(entry -> getValidSortAndFilterFields(uncapitalize(entry.getKey()), entry.getValue(), initialDepth))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterFields(String parentFieldPath,
      AbstractTypeConfiguration<?> typeConfiguration, int depth) {
    Map<String, ? extends AbstractFieldConfiguration> fields = typeConfiguration.getFields();

    return fields.values()
        .stream()
        .filter(field -> !field.isAggregateField())
        .map(field -> getValidSortAndFilterField(parentFieldPath, field, depth))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterField(String parentFieldPath, AbstractFieldConfiguration field,
      int depth) {
    String currentFieldPath = parentFieldPath.concat(".")
        .concat(field.getName());

    if (depth > MAX_DEPTH) {
      return List.of();
    }

    if ((currentFieldPath.contains(uncapitalize(field.getType()) + "."))) {
      return List.of(currentFieldPath);
    }

    if ((field.isNestedObjectField() || field.isObjectField())) {
      return getValidSortAndFilterFields(currentFieldPath, OBJECT_TYPES.get(field.getType()), depth + 1);
    }

    if (field.isScalarField()) {
      return List.of(currentFieldPath);
    }

    return List.of();
  }
}
