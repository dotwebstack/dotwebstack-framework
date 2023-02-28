package org.dotwebstack.framework.core.config.validators;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.getFieldKey;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class QueryValidator implements SchemaValidator {

  @Override
  public void validate(Schema schema) {
    var queries = schema.getQueries();

    queries.keySet()
        .forEach(queryName -> validateQuery(schema, queryName));
  }

  private void validateQuery(Schema schema, String queryName) {
    var query = schema.getQueries()
        .get(queryName);

    if (query.isBatch()) {
      if (query.isPageable()) {
        throw invalidConfigurationException("Batching for query '{}' in combination with paging is not supported!",
            queryName);
      }

      if (query.getKeys()
          .isEmpty()) {
        throw invalidConfigurationException("Batching for query '{}' without keys is not possible!", queryName);
      }

      if (query.getKeys()
          .size() > 1) {
        throw invalidConfigurationException("Batching for query '{}' with a composite key is not supported!",
            queryName);
      }
    }

    validateKeyAliases(query.getKeys());

    query.getKeyMap()
        .forEach((key, value) -> validateKeyValue(queryName, value, query.getType(), schema));
  }

  @SuppressWarnings("unchecked")
  private void validateKeyAliases(List<Object> keys) {
    var set = new HashSet<String>();

    for (Object key : keys) {
      String fieldKeyName;
      if (key instanceof String) {
        fieldKeyName = getFieldKey((String) key);
      } else if (key instanceof Map) {
        fieldKeyName = ((Map<String, String>) key).keySet()
            .stream()
            .findFirst()
            .orElseThrow();
      } else {
        throw invalidConfigurationException("Key must be an instance of String or Map.");
      }

      if (!set.contains(fieldKeyName)) {
        set.add(fieldKeyName);
      } else {
        throw invalidConfigurationException("Duplicate values are not allowed for keynames. Duplicate value: '{}'.",
            fieldKeyName);
      }
    }
  }

  private void validateKeyValue(String queryName, String keyPath, String objectTypeName, Schema schema) {
    var objectType = schema.getObjectType(objectTypeName)
        .orElseThrow(() -> invalidConfigurationException(
            "The type '{}', of query: '{}', doesn't exist in the configuration.", objectTypeName, queryName));

    validateKeyPath(keyPath, objectType, schema);
  }

  private void validateKeyPath(String fieldPath, ObjectType<?> objectType, Schema schema) {
    var splitKey = Arrays.asList(fieldPath.split("\\."));

    if (splitKey.size() > 3) {
      throw invalidConfigurationException("A key can't exist out of more than 3 fields. Key: '{}'.", fieldPath);
    }

    for (int i = 0; i < splitKey.size(); i++) {
      if (i == (splitKey.size() - 1)) {
        validateKeyField(schema, objectType.getName(), splitKey.get(i));
      } else {
        var fieldName = splitKey.get(i);
        validateObjectField(objectType, fieldName);

        ObjectType<?> finalObjectType = objectType;
        objectType = schema.getObjectType(objectType.getField(fieldName)
            .getType())
            .orElseThrow(() -> invalidConfigurationException(
                "The type '{}', of field: '{}', doesn't exist in the configuration.",
                finalObjectType.getField(fieldName)
                    .getType(),
                fieldName));
      }
    }
  }

  private void validateObjectField(ObjectType<?> objectType, String objectFieldName) {
    var objectField = objectType.getField(objectFieldName);
    validateField(objectField);
  }

  private void validateKeyField(Schema schema, String objectTypeName, String keyField) {
    var keyFieldPath = new String[] {keyField};
    Optional<? extends ObjectField> field = getField(schema, objectTypeName, keyFieldPath);

    if (field.isEmpty()) {
      throw invalidConfigurationException(
          "Key field '{}' in object type '{}' can't be resolved to a single scalar type.", keyFieldPath[0],
          objectTypeName);
    }

    validateField(field.get());
  }

  private void validateField(ObjectField objectField) {
    if (objectField.isNullable()) {
      throw invalidConfigurationException("A key can't contain fields that are nullable, for field: '{}'.",
          objectField.getName());
    }

    if (objectField.isList()) {
      throw invalidConfigurationException("A key can't contain fields that are a list, for field: '{}'.",
          objectField.getName());
    }
  }
}
