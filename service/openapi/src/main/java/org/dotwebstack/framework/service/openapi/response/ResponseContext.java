package org.dotwebstack.framework.service.openapi.response;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.dotwebstack.framework.core.query.GraphQlField;

@Getter
public class ResponseContext {

  private GraphQlField graphQlField;

  private List<ResponseTemplate> responses = new ArrayList<>();

  private List<Parameter> parameters = new ArrayList<>();

  public ResponseContext(GraphQlField graphQlField, List<ResponseTemplate> responses, List<Parameter> parameters) {
    this.graphQlField = graphQlField;
    this.responses = responses;
    this.parameters = parameters;
  }

  public Set<String> getRequiredResponseObjectsForSuccessResponse() {
    ResponseTemplate successResponse = responses.stream()
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

  private Map<String, ResponseObject> getRequiredResponseObject(String prefix, ResponseObject responseObject,
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

  private StringJoiner getStringJoiner(String prefix) {
    StringJoiner joiner = new StringJoiner(".");
    if (!prefix.isEmpty()) {
      joiner.add(prefix);
    }
    return joiner;
  }
}
