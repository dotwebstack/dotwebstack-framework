package org.dotwebstack.framework.service.openapi.mapping;

import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.response.ResponseObject;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;

class ResponseMapperHelper {

  private ResponseMapperHelper() {}

  static boolean isRequiredAndNullOrEmpty(@NonNull ResponseWriteContext writeContext, @NonNull Object object) {
    return writeContext.getSchema()
        .isRequired() && ((Objects.isNull(object)) || isEmptyList(writeContext.getSchema(), object));
  }

  private static boolean isEmptyList(ResponseObject responseObject, Object object) {
    if (responseObject.isNillable() && object instanceof List) {
      return ((List) object).isEmpty();
    }
    return false;
  }
}
