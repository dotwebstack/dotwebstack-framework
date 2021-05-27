package org.dotwebstack.framework.service.openapi.response;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class ResponseHeader {

  private final String name;

  private final String defaultValue;

  private final String type;

  private final Map<String, String> dwsExpressionMap;
}
