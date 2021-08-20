package org.dotwebstack.framework.service.openapi.mapping;

import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.response.ResponseWriteContext;
import org.dotwebstack.framework.service.openapi.response.oas.OasField;

class ResponseMapperHelper {

  private ResponseMapperHelper() {}

  static boolean isRequiredOrExpandedAndNullOrEmpty(@NonNull ResponseWriteContext writeContext, Object object,
      boolean expanded) {
    return (writeContext.getOasField()
        .isRequired() || expanded) && ((Objects.isNull(object)) || isEmptyList(writeContext.getOasField(), object));
  }

  private static boolean isEmptyList(OasField oasField, Object object) {
    if (oasField.isNillable() && object instanceof List) {
      return ((List<?>) object).isEmpty();
    }
    return false;
  }
}
