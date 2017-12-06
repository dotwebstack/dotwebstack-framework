package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

class RequestParameters {

  private final Map<String, Object> parameters = Maps.newHashMap();

  void putAll(MultivaluedMap<String, String> sourceParams) {
    for (String key : sourceParams.keySet()) {
      parameters.put(key, sourceParams.getFirst(key));
    }
  }

  RequestParameters putAll(Map<String, Object> sourceParams) {
    parameters.putAll(sourceParams);
    return this;
  }

  String asString(String paramName) {
    Object param = this.parameters.get(paramName);
    return param != null ? param.toString() : null;
  }

  Object get(String key) {
    return this.parameters.get(key);
  }

  @Override
  public String toString() {
    return parameters.toString();
  }
}
