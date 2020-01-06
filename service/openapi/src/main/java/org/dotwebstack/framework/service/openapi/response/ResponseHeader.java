package org.dotwebstack.framework.service.openapi.response;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseHeader {

  private String name;

  private String type;

  private String jexlExpression;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResponseHeader that = (ResponseHeader) o;
    return Objects.equals(name, that.name) && Objects.equals(type, that.type)
        && Objects.equals(jexlExpression, that.jexlExpression);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, jexlExpression);
  }
}
