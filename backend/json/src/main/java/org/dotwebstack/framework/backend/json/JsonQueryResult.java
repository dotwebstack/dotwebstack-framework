package org.dotwebstack.framework.backend.json;

import static com.jayway.jsonpath.Criteria.where;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import net.minidev.json.JSONArray;
import org.dotwebstack.framework.core.datafetchers.FieldKeyCondition;

@Getter
public final class JsonQueryResult {

  private final JsonNode jsonNode;

  private final String jsonPathTemplate;

  public JsonQueryResult(JsonNode jsonNode, String jsonPathTemplate) {
    this.jsonNode = jsonNode;
    this.jsonPathTemplate = jsonPathTemplate;
  }

  public List<Map<String, Object>> getResults(FieldKeyCondition fieldKeyCondition) {
    List<com.jayway.jsonpath.Filter> jsonPathFilters = fieldKeyCondition.getFieldValues()
        .entrySet()
        .stream()
        .map(this::createFilter)
        .collect(Collectors.toList());

    JSONArray jsonPathResult = getJsonPathResult(jsonPathFilters, jsonPathTemplate);

    if (jsonPathResult.isEmpty()) {
      return emptyList();
    }

    return getResultList(jsonPathResult);
  }

  public Optional<Map<String, Object>> getResult(FieldKeyCondition fieldKeyCondition) {
    List<Map<String, Object>> resultList = getResults(fieldKeyCondition);

    if (resultList.isEmpty()) {
      return empty();
    }

    if (resultList.size() > 1) {
      throw illegalStateException(String.format("Found %s values, expected max one.", resultList.size()));
    }

    return resultList.stream()
        .findFirst();
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getResultList(JSONArray jsonPathResult) {
    return jsonPathResult.stream()
        .flatMap(subject -> {
          if (subject instanceof JSONArray) {
            return ((JSONArray) subject).stream();
          }
          return Stream.of(subject);
        })
        .map(obj -> (Map<String, Object>) obj)
        .collect(Collectors.toList());
  }

  private JSONArray getJsonPathResult(List<com.jayway.jsonpath.Filter> jsonPathFilters, String jsonPathTemplate) {
    return JsonPath.parse(jsonNode.toString())
        .read(jsonPathTemplate, jsonPathFilters.toArray(new com.jayway.jsonpath.Filter[jsonPathFilters.size()]));
  }

  private com.jayway.jsonpath.Filter createFilter(Map.Entry<String, Object> key) {
    return Optional.of(key)
        .map(entry -> where(entry.getKey()).is(entry.getValue()))
        .map(com.jayway.jsonpath.Filter::filter)
        .orElseThrow(() -> illegalStateException("Unable to create filter for key!"));
  }
}
