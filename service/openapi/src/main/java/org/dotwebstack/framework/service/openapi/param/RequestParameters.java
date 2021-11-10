package org.dotwebstack.framework.service.openapi.param;

import java.util.HashMap;
import java.util.Optional;

public class RequestParameters {

  private HashMap<String, Object> pathParameters;

  private HashMap<String, Object> queryParameters;

  private HashMap<String, Object> bodyParameters;

  public enum Source {
    QUERY, PATH, BODY
  }

  public void addParameter(String key, Source source) {

  }

  public Optional<Object> getParameter(String key, Source source) {
    switch (source) {
      case QUERY:
        return Optional.ofNullable(queryParameters.get(key));
      case PATH:
        return Optional.ofNullable(pathParameters.get(key));
      case BODY:
        return Optional.ofNullable(bodyParameters.get(key));
      default:
        throw new IllegalStateException();
    }
  }
}
