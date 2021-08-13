package org.dotwebstack.framework.service.openapi.response;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;


@Builder
@Getter
public class ResponseObject {

  private final String identifier;

  private final ResponseObject parent;

  @Setter
  private SchemaSummary summary;

  public boolean isComposedOf() {
    return !getSummary().getComposedOf()
        .isEmpty();
  }

  public boolean isArray() {
    return parent != null && OasConstants.ARRAY_TYPE.equals(parent.getSummary()
        .getType());
  }

  public boolean isScalar() {
    return !Set.of(OasConstants.ARRAY_TYPE, OasConstants.OBJECT_TYPE)
        .contains(summary.getType());
  }
}
