package org.dotwebstack.framework.core.config.validators;

import static org.springframework.util.StringUtils.uncapitalize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;

@Slf4j
public final class ValidSortAndFilterFields {

  private static final int MAX_DEPTH = 10;

  private ValidSortAndFilterFields() {}

  public static List<String> get(Schema schema) {
    return get(schema, 0);
  }

  public static List<String> get(Schema schema, int initialDepth) {
    Map<String, ObjectType<?>> objectTypes = schema.getObjectTypes();

    return objectTypes.entrySet()
        .stream()
        .map(entry -> getValidSortAndFilterFields(objectTypes, uncapitalize(entry.getKey()), entry.getValue(),
            initialDepth))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterFields(Map<String, ObjectType<?>> objectTypes,
      String parentFieldPath, ObjectType<?> objectType, int depth) {
    Map<String, ? extends ObjectField> fields = objectType.getFields();

    return fields.values()
        .stream()
        // .filter(field -> !field.isAggregateField())
        .map(field -> getValidSortAndFilterField(objectTypes, parentFieldPath, field, depth))
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private static List<String> getValidSortAndFilterField(Map<String, ObjectType<?>> objectTypes, String parentFieldPath,
      ObjectField objectField, int depth) {
    String currentFieldPath = parentFieldPath.concat(".")
        .concat(objectField.getName());

    if (depth > MAX_DEPTH) {
      return List.of();
    }

    // TODO: Fix me. Hier zijn al methodes voor in de ModelConfiguration die op een algemene plek
    // bevraagbaar moeten worden

    // if ((objectField.isNestedObjectField() || objectField.isObjectField())) {
    // return getValidSortAndFilterFields(objectTypes, currentFieldPath,
    // objectTypes.get(objectField.getType()), depth + 1);
    // }
    //
    // if (objectField.isScalarField()) {
    // return List.of(currentFieldPath);
    // }

    return List.of();
  }
}
