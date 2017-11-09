package org.dotwebstack.framework.frontend.openapi.entity.builder;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.MultivaluedMap;

public class RequestParameters {

  private Map<String, Object> parameters = Maps.newHashMap();


  public RequestParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public void putAll(MultivaluedMap<String, String> sourceParams) {
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

    public Builder() {

    }

    public Builder requestStringParameters(Map<String, String> requestParameters) {
      Map<String,String> newMap = requestParameters.entrySet().stream()
              .collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue()));

      this.requestParameters.putAll(newMap);
      return this;
    }



    public Builder requestObjectParameters(Map<String, Object> requestParameters) {
      this.requestParameters.putAll(requestParameters);
      return this;
    }

    public RequestParameters build() {
      return new RequestParameters(this.requestParameters);
    }
  }

}
