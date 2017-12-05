package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

// XXX (PvH) Kan de klasse package-private worden? Dit geldt ook voor de methods.
// XXX (PvH) Er ontbreekt testdekking op de klasse
public class RequestParameters {

  private final Map<String, Object> parameters = Maps.newHashMap();

  void putAll(MultivaluedMap<String, String> sourceParams) {
    for (String key : sourceParams.keySet()) {
      parameters.put(key, sourceParams.getFirst(key));
    }
  }

  public RequestParameters putAll(Map<String, Object> sourceParams) {
    parameters.putAll(sourceParams);
    return this;
  }

  RequestParameters putAll(RequestParameters requestParameters) {
    this.parameters.putAll(requestParameters.parameters);
    return this;
  }

  void putIfAbsent(String paramName, Object value) {
    this.parameters.putIfAbsent(paramName, value);
  }

  public String asString(String paramName) {
    Object param = this.parameters.get(paramName);
    return param != null ? param.toString() : null;
  }

  public Integer asInt(String paramName, Integer defaultValue) {
    Object param = this.parameters.get(paramName);
    return param != null ? Integer.valueOf(param.toString()) : defaultValue;
  }

  public Integer asInt(String paramName) {
    return asInt(paramName, null);
  }

  public boolean asBoolean(String paramName) {
    Object paramValue = this.parameters.get(paramName);
    if (paramValue == null) {
      return false;
    }

    return Boolean.valueOf(paramValue.toString());
  }

  Map<String, Object> asMap() {
    return Collections.unmodifiableMap(this.parameters);
  }

  public void put(String paramName, Object value) {
    this.parameters.put(paramName, value);
  }

  void cleanParameters(String... paramNames) {
    if (paramNames == null || paramNames.length == 0) {
      return;
    }

    for (String paramName : paramNames) {
      this.parameters.remove(paramName);
    }
  }

  @Override
  public String toString() {
    return parameters.toString();
  }

  public Object get(String key) {
    return this.parameters.get(key);
  }
}
