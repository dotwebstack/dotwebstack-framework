package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import lombok.NonNull;

class RequestParameters {

  private final Map<String, String> parameters = Maps.newHashMap();
  private String rawBody;

  void putAll(@NonNull MultivaluedMap<String, String> sourceParams) {
    for (String key : sourceParams.keySet()) {
      put(key, sourceParams.getFirst(key));
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
