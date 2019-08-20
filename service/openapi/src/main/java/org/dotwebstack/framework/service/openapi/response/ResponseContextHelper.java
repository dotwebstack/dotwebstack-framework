package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;

public class ResponseContextHelper {

  public static Set<String> getRequiredResponseObjectsForSuccessResponse(ResponseContext responseContext) {
    ResponseTemplate successResponse = responseContext.getResponses()
        .stream()
        .filter(template -> template.isApplicable(200, 299))
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException("No success response found for ResponseContext!"));

    return getRequiredResponseObject("", successResponse.getResponseObject(), true).keySet()
        .stream()
        .map(path -> {
          String[] pathArray = path.split("\\.");
          return String.join(".", ArrayUtils.removeElement(pathArray, pathArray[0]));
        })
        .filter(path -> !path.isEmpty())
        .collect(Collectors.toSet());
  }

  private static Map<String, ResponseObject> getRequiredResponseObject(String prefix, ResponseObject responseObject,
      boolean isChild) {
    Map<String, ResponseObject> responseObjects = new HashMap<>();
    StringJoiner joiner = getStringJoiner(prefix);

    if (isChild && !responseObject.isEnvelope() && !responseObject.getIdentifier()
        .startsWith("#")) {
      joiner.add(responseObject.getIdentifier());
    }

    if (responseObject.isRequired() && (!responseObject.isEnvelope() && !responseObject.getIdentifier()
        .startsWith("#"))) {
      responseObjects.put(joiner.toString(), responseObject);
    }

    if (!responseObject.getChildren()
        .isEmpty()) {
      responseObjects.putAll(responseObject.getChildren()
          .stream()
          .flatMap(child -> getRequiredResponseObject(joiner.toString(), child, true).entrySet()
              .stream())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    if (!responseObject.getItems()
        .isEmpty()) {
      responseObjects.putAll(responseObject.getItems()
          .stream()
          .flatMap(item -> getRequiredResponseObject(joiner.toString(), item, false).entrySet()
              .stream())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    return responseObjects;
  }

  private static StringJoiner getStringJoiner(String prefix) {
    StringJoiner joiner = new StringJoiner(".");
    if (!prefix.isEmpty()) {
      joiner.add(prefix);
    }
    return joiner;
  }

}
