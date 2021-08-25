package org.dotwebstack.framework.service.openapi.query.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.service.openapi.query.FieldHelper.resolveField;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlFilter;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.dwssettings.QueryFilter;

public class FilterHelper {

  private FilterHelper() {}

  public static void addKeys(@NonNull GraphQlQuery query, @NonNull Map<String, String> keyMap,
      @NonNull Map<String, Object> inputParams) {
    Set<Key> keys = getKeys(keyMap, inputParams);

    keys.forEach(key -> {
      String[] path = key.getFieldPath()
          .split("\\.");
      Field field = resolveField(query, path);
      field.getArguments()
          .put(path[path.length - 1], key.getValue());
    });
  }

  public static Map<String, Object> addFilters(@NonNull GraphQlQuery query, @NonNull List<QueryFilter> filters,
      @NonNull Map<String, Object> inputParams) {
    Map<String, Object> result = new HashMap<>();
    for (int i = 0; i < filters.size(); i++) {
      QueryFilter filter = filters.get(i);
      GraphQlFilter.GraphQlFilterBuilder builder = GraphQlFilter.builder();
      Map<?, ?> fieldFilters = filter.getFieldFilters();
      fieldFilters = resolveVariables(fieldFilters, inputParams);

      if (fieldFilters != null) {
        builder.content(fieldFilters);

        String filterId = "filter" + i;
        result.put(filterId, fieldFilters);
        query.getVariables()
            .put(filterId, filter.getType());

        String[] path = filter.getFieldPath();
        Field field = resolveField(query, path);
        field.setFilterId(filterId);
      }
    }

    return result;
  }

  private static Map<?, ?> resolveVariables(Map<?, ?> tree, Map<String, Object> inputParams) {
    if (tree == null) {
      return Collections.emptyMap();
    }

    Map<?, ?> result = tree.entrySet()
        .stream()
        .map(e -> {
          Object value = e.getValue();
          boolean required = false;
          if (value instanceof String && ((String) value).startsWith("$")) {
            String[] split = ((String) value).split("\\.");
            String name = split[split.length - 1];
            if (name.endsWith("!")) {
              required = true;
            }
            value = getObject(inputParams, Arrays.copyOfRange(split, 1, split.length));
          } else if (value instanceof Map) {
            value = resolveVariables((Map<?, ?>) value, inputParams);
          }
          if (required && value == null) {
            return null;
          } else {
            return value != null ? Map.entry(e.getKey(), value) : null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return result.isEmpty() ? null : result;
  }

  private static Object getObject(Map<String, ?> map, String[] path) {
    Map<String, ?> search = map;
    Object result = null;
    for (int i = 0; i < path.length; i++) {
      String pathEntry = path[i];
      if (pathEntry.endsWith("!")) {
        pathEntry = pathEntry.substring(0, pathEntry.length() - 1);
      }
      result = search.get(pathEntry);

      if (result instanceof Map<?, ?> && i < path.length - 1) {
        search = (Map<String, ?>) result;
      } else if (result != null && i < path.length - 1) {
        throw illegalStateException("path item {} from path {} does not point to a map", pathEntry, path);
      }
    }
    return result;
  }

  private static Set<Key> getKeys(Map<String, String> keyMap, Map<String, Object> inputParams) {
    return keyMap.entrySet()
        .stream()
        .map(e -> {
          String path = e.getKey();
          String paramName = e.getValue();
          String[] parts = paramName.split("\\.");
          Object paramValue = inputParams.get(parts[1]);

          return paramValue != null ? Key.builder()
              .fieldPath(path)
              .value(paramValue)
              .build() : null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

}
