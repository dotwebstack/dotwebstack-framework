package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.query.GraphQlField;

public class ResponseContextHelper {

  public static Set<String> getRequiredResponseObjectsForSuccessResponse(ResponseContext responseContext) {
    ResponseTemplate successResponse = responseContext.getResponses()
        .stream()
        .filter(template -> template.isApplicable(200, 299))
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException("No success response found for ResponseContext!"));

    return getRequiredResponseObject("", successResponse.getResponseObject(), responseContext.getGraphQlField())
        .keySet()
        .stream()
        .filter(path -> !path.isEmpty())
        .collect(Collectors.toSet());
  }

  private static Map<String, ResponseObject> getRequiredResponseObject(String prefix, ResponseObject responseObject,
      GraphQlField graphQlField) {
    Map<String, ResponseObject> responseObjects = new HashMap<>();
    StringJoiner joiner = getStringJoiner(prefix);

    GraphQlField childField = graphQlField.getFields()
        .stream()
        .filter(field -> field.getName()
            .equals(responseObject.getIdentifier()))
        .findFirst()
        .orElse(graphQlField);

    if (!responseObject.isEnvelope() && !childField.equals(graphQlField)) {
      joiner.add(responseObject.getIdentifier());
      responseObjects.put(joiner.toString(), responseObject);
    }

    if (!responseObject.getChildren()
        .isEmpty()) {
      responseObjects.putAll(responseObject.getChildren()
          .stream()
          .flatMap(child -> getRequiredResponseObject(joiner.toString(), child, childField).entrySet()
              .stream())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    if (!responseObject.getItems()
        .isEmpty()) {
      responseObjects.putAll(responseObject.getItems()
          .stream()
          .flatMap(item -> getRequiredResponseObject(joiner.toString(), item, graphQlField).entrySet()
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
