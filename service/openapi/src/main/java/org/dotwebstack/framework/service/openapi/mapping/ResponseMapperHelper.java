package org.dotwebstack.framework.service.openapi.mapping;

import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.dotwebstack.framework.service.openapi.response.SchemaSummary;

class ResponseMapperHelper {

  private ResponseMapperHelper() {}

  static boolean isRequiredAndNullOrEmpty(@NonNull ResponseWriteContext writeContext, Object object) {
    return writeContext.getResponseObject()
        .getSummary()
        .isRequired()
        && ((Objects.isNull(object)) || isEmptyList(writeContext.getResponseObject()
            .getSummary(), object));
  }

  private static boolean isEmptyList(SchemaSummary responseSchema, Object object) {
    if (responseSchema.isNillable() && object instanceof List) {
      return ((List) object).isEmpty();
    }
    return false;
  }
}
