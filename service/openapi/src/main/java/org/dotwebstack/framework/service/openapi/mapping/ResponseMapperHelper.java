package org.dotwebstack.framework.service.openapi.mapping;

import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.response.ResponseSchema;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;

class ResponseMapperHelper {

  private ResponseMapperHelper() {}

  static boolean isRequiredAndNullOrEmpty(@NonNull ResponseWriteContext writeContext, Object object) {
    return writeContext.getResponseObject()
        .getSchema()
        .isRequired()
        && ((Objects.isNull(object)) || isEmptyList(writeContext.getResponseObject()
            .getSchema(), object));
  }

  private static boolean isEmptyList(ResponseSchema responseSchema, Object object) {
    if (responseSchema.isNillable() && object instanceof List) {
      return ((List) object).isEmpty();
    }
    return false;
  }
}
