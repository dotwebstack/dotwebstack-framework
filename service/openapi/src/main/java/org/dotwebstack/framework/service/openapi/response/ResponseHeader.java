package org.dotwebstack.framework.service.openapi.response;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class ResponseHeader {

  private String name;

  private String defaultValue;

  private String type;

  private Map<String, String> dwsExpressionMap;
}
