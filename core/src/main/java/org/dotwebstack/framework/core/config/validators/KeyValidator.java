package org.dotwebstack.framework.core.config.validators;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class KeyValidator implements SchemaValidator {

  private Set<String> keyNames = new HashSet<>();

  @Override
  public void validate(Schema schema) {
    var queries = schema.getQueries();

    queries.forEach((queryName, query) -> query.getKeys()
        .forEach(keyPath -> validateKeyFieldPath(queryName, keyPath, query.getType(), schema)));
  }

  private void validateKeyFieldPath(String queryName, String keyPath, String objectTypeName, Schema schema) {
    Optional<ObjectType<?>> objectType = schema.getObjectType(objectTypeName);

    if (objectType.isEmpty()) {
      throw invalidConfigurationException("The type '{}', of query: '{}', doesn't exist in the configuration.",
          objectTypeName, queryName);
    }

    if (keyPath.contains(".")) {
      var splittedKey = Arrays.asList(keyPath.split("\\.", 2));
      var nestedFieldName = splittedKey.get(0);
      var nestedFieldTypeName = objectType.get()
          .getField(nestedFieldName)
          .getType();

      validateObjectField(objectType.get(), nestedFieldName);

      var key = splittedKey.get(1);
      validateKeyFieldPath(queryName, key, nestedFieldTypeName, schema);
    } else {
      validateKey(schema, objectType.get()
          .getName(), keyPath);
      keyNames.add(keyPath);
    }
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
      throw invalidConfigurationException("A key can't contain fields that are nullable.");
    }

    if (objectField.isList()) {
      throw invalidConfigurationException("A key can't contain fields that are a list.");
    }
  }
}
