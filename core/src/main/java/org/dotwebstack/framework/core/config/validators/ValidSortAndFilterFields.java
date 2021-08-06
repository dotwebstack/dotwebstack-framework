package org.dotwebstack.framework.core.config.validators;

import static org.springframework.util.StringUtils.uncapitalize;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;

@Slf4j
public final class ValidSortAndFilterFields {

  private ValidSortAndFilterFields() {}

  public static List<String> get(DotWebStackConfiguration dotWebStackConfiguration) {
    Map<String, AbstractTypeConfiguration<?>> objectTypes = dotWebStackConfiguration.getObjectTypes();

    return objectTypes.entrySet()
        .stream()
        .map(entry -> getValidSortAndFilterFields(objectTypes, uncapitalize(entry.getKey()), entry.getValue(),
            new HashSet<>()))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterFields(Map<String, AbstractTypeConfiguration<?>> objectTypes,
      String parentFieldPath, AbstractTypeConfiguration<?> typeConfiguration, Set<String> processed) {
    Map<String, ? extends AbstractFieldConfiguration> fields = typeConfiguration.getFields();

    return fields.values()
        .stream()
        .filter(field -> !field.isList())
        .filter(field -> !field.isAggregateField())
        .map(field -> getValidSortAndFilterField(objectTypes, parentFieldPath, field, processed))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterField(Map<String, AbstractTypeConfiguration<?>> objectTypes,
      String parentFieldPath, AbstractFieldConfiguration field, Set<String> processed) {
    String currentFieldPath = parentFieldPath.concat(".")
        .concat(field.getName());

    if ((field.isNestedObjectField() || field.isObjectField()) && !processed.contains(field.getType())) {
      processed.add(field.getType());
      return getValidSortAndFilterFields(objectTypes, currentFieldPath, objectTypes.get(field.getType()), processed);
    } else {
      return List.of(currentFieldPath);
    }
  }
}
