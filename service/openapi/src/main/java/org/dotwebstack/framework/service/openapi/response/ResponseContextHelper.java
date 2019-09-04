package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;

public class ResponseContextHelper {

  private ResponseContextHelper() {}

  public static Set<String> getRequiredResponseObjectsForSuccessResponse(
      @NonNull ResponseSchemaContext responseSchemaContext) {
    ResponseTemplate successResponse = responseSchemaContext.getResponses()
        .stream()
        .filter(template -> template.isApplicable(200, 299))
        .findFirst()
        .orElseThrow(() -> invalidConfigurationException("No success response found for ResponseSchemaContext!"));

    return getRequiredResponseObject("", successResponse.getResponseObject(), responseSchemaContext.getGraphQlField())
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

    if (!responseObject.isEnvelope() && responseObject.isRequired() && !childField.equals(graphQlField)
        && !Objects.equals(responseObject.getType(), OasConstants.ARRAY_TYPE)) {
      joiner.add(responseObject.getIdentifier());
      responseObjects.put(joiner.toString(), responseObject);
    }

    if (!responseObject.getChildren()
        .isEmpty()) {
      responseObject.getChildren()
          .stream()
          .flatMap(child -> getRequiredResponseObject(joiner.toString(), child, childField).entrySet()
              .stream())
          .forEach(entry -> responseObjects.put(entry.getKey(), entry.getValue()));
    }

    if (!responseObject.getItems()
        .isEmpty()) {
      responseObject.getItems()
          .stream()
          .flatMap(item -> getRequiredResponseObject(joiner.toString(), item, graphQlField).entrySet()
              .stream())
          .forEach(entry -> responseObjects.put(entry.getKey(), entry.getValue()));
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
