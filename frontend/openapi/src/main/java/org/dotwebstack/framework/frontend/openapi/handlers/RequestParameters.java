package org.dotwebstack.framework.frontend.openapi.handlers;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import lombok.NonNull;

class RequestParameters {

  private final Map<String, String> parameters;
  private String rawBody;

  RequestParameters() {
    parameters = new HashMap<>();
  }

  void putAll(@NonNull MultivaluedMap<String, String> parameters) {
    for (String key : parameters.keySet()) {
      put(key, parameters.getFirst(key));
    }
  }

  void put(@NonNull String key, String value) {
    parameters.put(key, value);
  }

  String get(@NonNull String key) {
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
