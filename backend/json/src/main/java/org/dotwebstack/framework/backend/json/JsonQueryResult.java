package org.dotwebstack.framework.backend.json;

import static com.jayway.jsonpath.Criteria.where;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import net.minidev.json.JSONArray;
import org.dotwebstack.framework.backend.json.config.JsonTypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.keys.FieldKey;

@Getter
public final class JsonQueryResult {

  private final JsonNode jsonNode;

  private final JsonTypeConfiguration typeConfiguration;

  public JsonQueryResult(JsonNode jsonNode, JsonTypeConfiguration typeConfiguration) {
    this.jsonNode = jsonNode;
    this.typeConfiguration = typeConfiguration;
  }

  public List<Map<String, Object>> getResults() {
    List<Filter> jsonPathFilters = new ArrayList<>();

    String jsonPathTemplate = typeConfiguration.getPath();

    JSONArray jsonPathResult = getJsonPathResult(jsonPathFilters, jsonPathTemplate);

    if (jsonPathResult.isEmpty()) {
      return emptyList();
    }

    return getResultList(jsonPathResult);
  }

  public Optional<Map<String, Object>> getResult(Object key) {
    if (!(key instanceof FieldKey)) {
      throw illegalArgumentException("Unsupported key");
    }

    FieldKey fieldKey = ((FieldKey) key);
    String jsonPathTemplate = String.format("%s%s", typeConfiguration.getPath(), "[?]");

    List<Filter> jsonPathFilters = createJsonPathWithArguments(fieldKey);

    JSONArray jsonPathResult = getJsonPathResult(jsonPathFilters, jsonPathTemplate);

    List<Map<String, Object>> resultList = getResultList(jsonPathResult);

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

  private JSONArray getJsonPathResult(List<Filter> jsonPathFilters, String jsonPathTemplate) {
    return JsonPath.parse(jsonNode.toString())
        .read(jsonPathTemplate, jsonPathFilters.toArray(new Filter[jsonPathFilters.size()]));
  }

  private List<Filter> createJsonPathWithArguments(FieldKey fieldKey) {
    return List.of(fieldKey)
        .stream()
        .map(predicateFilter -> where(fieldKey.getName()).is(fieldKey.getValue()))
        .map(Filter::filter)
        .collect(Collectors.toList());
  }
}
