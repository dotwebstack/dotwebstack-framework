package org.dotwebstack.framework.service.openapi.query.filter;

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
      String[] fieldPath = path.length <= 1 ? new String[] {} : Arrays.copyOfRange(path, 0, path.length - 1);
      Field field = resolveField(query, fieldPath);
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
        String[] fieldPath = path.length <= 1 ? new String[] {} : Arrays.copyOfRange(path, 0, path.length - 1);
        Field field = resolveField(query, fieldPath);
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
          if (value instanceof String && ((String) value).startsWith("$")) {
            String[] split = ((String) value).split("\\.");
            String name = split[1];
            value = inputParams.get(name);
          } else if (value instanceof Map) {
            value = resolveVariables((Map<?, ?>) value, inputParams);
          }

          return value != null ? Map.entry(e.getKey(), value) : null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return result.isEmpty() ? null : result;
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
