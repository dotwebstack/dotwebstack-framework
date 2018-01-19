package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import lombok.NonNull;

class RequestParameters {

  private final Map<String, Object> parameters = Maps.newHashMap();
  private String rawBody;

  void putAll(@NonNull MultivaluedMap<String, String> sourceParams) {
    for (String key : sourceParams.keySet()) {
      put(key, sourceParams.getFirst(key));
    }
  }

  void put(@NonNull String key, Object value) {
    parameters.put(key, value);
  }

  String asString(@NonNull String paramName) {
    Object param = get(paramName);
    return param != null ? param.toString() : null;
  }

  Object get(@NonNull String key) {
    return this.parameters.get(key);
  }

  @Override
  public String toString() {
    return parameters.toString();
  }

  public String getRawBody() {
    return rawBody;
  }

  public void setRawBody(String rawBody) {
    this.rawBody = rawBody;
  }
}
