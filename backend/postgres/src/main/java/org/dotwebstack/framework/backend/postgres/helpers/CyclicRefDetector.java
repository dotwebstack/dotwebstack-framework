package org.dotwebstack.framework.backend.postgres.helpers;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;

@Slf4j
public class CyclicRefDetector {

  private final List<String> processedFields;

  public CyclicRefDetector() {
    processedFields = new ArrayList<>();
  }

  public boolean isProcessed(PostgresObjectType objectType, PostgresObjectField objectField) {
    var fieldName = generateFieldName(objectType, objectField);
    if (processedFields.contains(fieldName)) {
      LOG.warn("Cyclic reference detected! Field {} is already processed.", fieldName);
      return true;
    }
    LOG.trace("Field {} has been processed and is not part of a Cyclic reference.", fieldName);
    processedFields.add(fieldName);
    return false;
  }

  private String generateFieldName(PostgresObjectType objectType, PostgresObjectField objectField) {
    return String.format("%s__%s__%s", objectType.getName(), objectField.getName(), objectField.getTargetType()
        .getName());
  }
}
