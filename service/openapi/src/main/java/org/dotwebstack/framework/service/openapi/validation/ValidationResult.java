package org.dotwebstack.framework.service.openapi.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class ValidationResult {
  List<String> globalErrors = new ArrayList<>();

  Map<String, List<String>> queryErrors = new HashMap<>();

  public void addQueryError(String queryName, String method, String errorMessage) {
    var key = getKey(queryName, method);
    var errors = queryErrors.getOrDefault(key, new ArrayList<>());
    errors.add(errorMessage);
    queryErrors.put(key, errors);
  }

  private static String getKey(String queryName, String method) {
    return queryName + "." + method.toLowerCase();
  }

  public boolean hasErrors() {
    return !globalErrors.isEmpty() || !queryErrors.isEmpty();
  }
}
