package org.dotwebstack.framework.core.config.validators;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.isNestedFieldPath;

import java.util.Arrays;
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

    if (query.isBatch() && query.isPageable()) {
      throw invalidConfigurationException("Paging and batching is not supported for query '{}'!", queryName);
    }

    query.getKeys()
        .forEach(keyPath -> validateKeyFieldPath(queryName, keyPath, query.getType(), schema));
  }

  private void validateKeyFieldPath(String queryName, String keyPath, String objectTypeName, Schema schema) {
    ObjectType<?> objectType;
    if (schema.getObjectType(objectTypeName)
        .isEmpty()) {
      throw invalidConfigurationException("The type '{}', of query: '{}', doesn't exist in the configuration.",
          objectTypeName, queryName);
    } else {
      objectType = schema.getObjectType(objectTypeName)
          .orElseThrow();
    }

    if (isNestedFieldPath(keyPath)) {
      validateComposedKeyField(queryName, keyPath, objectType, schema);
    } else {
      validateKey(schema, objectType.getName(), keyPath);
    }
  }

  private void validateComposedKeyField(String queryName, String composedKey, ObjectType<?> objectType, Schema schema) {
    var splittedKey = Arrays.asList(composedKey.split("\\.", 2));
    var nestedFieldName = splittedKey.get(0);
    var nestedFieldTypeName = objectType.getField(nestedFieldName)
        .getType();

    validateObjectField(objectType, nestedFieldName);

    var key = splittedKey.get(1);
    validateKeyFieldPath(queryName, key, nestedFieldTypeName, schema);
  }

  private void validateObjectField(ObjectType<?> objectType, String objectFieldName) {
    var objectField = objectType.getField(objectFieldName);
    validateField(objectField);
  }

  private void validateKey(Schema schema, String objectTypeName, String keyField) {

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
