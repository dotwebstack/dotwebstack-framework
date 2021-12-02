package org.dotwebstack.framework.core.config.validators;


import java.util.Arrays;
import java.util.Optional;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.Schema;

public interface SchemaValidator {

  void validate(Schema schema);

  default Optional<ObjectField> getField(Schema schema, String objectTypeName, String[] fieldPath) {
    return schema.getObjectType(objectTypeName)
        .map(type -> type.getFields()
            .get(fieldPath[0]))
        .flatMap(field -> {
          if (isNestedNode(field, fieldPath.length)) {
            // throw invalidConfigurationException("Filter field '{}' in object type '{}' with multiple nested
            // objects!",
            // String.join(".", fieldPath), objectTypeName);
            return Optional.empty();
          }
          if (fieldPath.length > 1) {
            return getField(schema, field.getType(), Arrays.copyOfRange(fieldPath, 1, fieldPath.length));
          } else {
            return Optional.of(field);
          }
        });
  }

  private boolean isNestedNode(ObjectField objectField, Integer fieldPathLength) {
    if (objectField.getTargetType() == null) {
      return false;
    }

    return objectField.getTargetType()
        .isNested() && fieldPathLength > 2;
  }
}
