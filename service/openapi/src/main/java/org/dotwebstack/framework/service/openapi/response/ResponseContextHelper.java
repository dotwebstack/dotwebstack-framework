package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_ENVELOPE;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPANDED_PARAMS;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

public class ResponseContextHelper {

  private ResponseContextHelper() {
  }

  public static String getPathString(String prefix, ResponseObject responseObject) {
    var expandJoiner = new StringJoiner(".");
    if (!prefix.isBlank()) {
      expandJoiner.add(prefix);
    }
    expandJoiner.add(responseObject.getIdentifier());
    return expandJoiner.toString();
  }

  private static StringJoiner getStringJoiner(String prefix) {
    var joiner = new StringJoiner(".");
    if (!prefix.isEmpty()) {
      joiner.add(prefix);
    }
    return joiner;
  }

  @SuppressWarnings("unchecked")
  public static boolean isExpanded(Map<String, Object> inputParams, @NonNull String path) {
    if (Objects.isNull(inputParams)) {
      return false;
    }
    List<String> expandVariables = (List<String>) inputParams.get(X_DWS_EXPANDED_PARAMS);
    if (Objects.nonNull(expandVariables)) {
      return expandVariables.stream()
          .anyMatch(path::equals);
    }
    return false;
  }

  public static boolean isCollection(ResponseObject responseObject) {
    if (responseObject.getSummary().hasExtension(X_DWS_ENVELOPE)) {
      return isCollection(responseObject.getSummary()
          .getChildren()
          .get(0));
    } else {
      return responseObject.getSummary().getSchema().getType().equals("array");
    }
  }
}
