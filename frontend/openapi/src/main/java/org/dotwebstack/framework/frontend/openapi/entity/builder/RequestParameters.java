package org.dotwebstack.framework.frontend.openapi.entity.builder;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Collectors;

public class RequestParameters {

  private Map<String, Object> parameters = Maps.newHashMap();

  RequestParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  RequestParameters putAll(RequestParameters requestParameters) {
    this.parameters.putAll(requestParameters.parameters);
    return this;
  }

  @Override
  public String toString() {
    return parameters.toString();
  }

  public Object get(String key) {
    return this.parameters.get(key);
  }


  public static RequestParameters.Builder builder() {
    return new RequestParameters.Builder();
  }


  public static class Builder {

    private Map<String, Object> requestParameters = Maps.newHashMap();

    public Builder requestStringParameters(Map<String, String> requestParameters) {
      Map<String, String> newMap = requestParameters.entrySet().stream().collect(
          Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));

      this.requestParameters.putAll(newMap);
      return this;
    }


    public RequestParameters build() {
      return new RequestParameters(this.requestParameters);
    }
  }

}
