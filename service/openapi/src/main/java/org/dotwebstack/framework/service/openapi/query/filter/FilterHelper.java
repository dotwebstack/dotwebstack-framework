package org.dotwebstack.framework.service.openapi.query.filter;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.service.openapi.helper.OasConstants.X_DWS_EXPR;
import static org.dotwebstack.framework.service.openapi.query.FieldHelper.resolveField;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.query.model.Field;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlFilter;
import org.dotwebstack.framework.service.openapi.query.model.GraphQlQuery;
import org.dotwebstack.framework.service.openapi.response.dwssettings.QueryFilter;

public class FilterHelper {

  private final JexlHelper jexlHelper;

  private final JexlContext jexlContext;

  private final Map<String, Object> inputParams;

  public FilterHelper(@NonNull JexlEngine jexlEngine, @NonNull Map<String, Object> inputParams) {
    this.jexlHelper = new JexlHelper(jexlEngine);
    this.inputParams = inputParams;
    this.jexlContext = new MapContext();
    initJexlContext();
  }

  public void addKeys(@NonNull GraphQlQuery query, @NonNull Map<String, String> keyMap) {
    Set<Key> keys = getKeys(keyMap, inputParams);

    keys.forEach(key -> {
      String[] path = key.getFieldPath()
          .split("\\.");
      Field field = resolveField(query, path);
      field.getArguments()
          .put(path[path.length - 1], key.getValue());
    });
  }

  public Map<String, Object> addFilters(@NonNull GraphQlQuery query, @NonNull List<QueryFilter> filters) {
    Map<String, Object> result = new HashMap<>();
    for (int i = 0; i < filters.size(); i++) {
      QueryFilter filter = filters.get(i);
      GraphQlFilter.GraphQlFilterBuilder builder = GraphQlFilter.builder();
      Map<?, ?> fieldFilters = filter.getFieldFilters();
      fieldFilters = resolveVariables(fieldFilters);

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

  private Map<?, ?> resolveVariables(Map<?, ?> tree) {
    if (tree == null) {
      return Collections.emptyMap();
    }
    Map<Object, Object> result = new TreeMap<>();
    for (Map.Entry<?, ?> entry : tree.entrySet()) {
      Object value = entry.getValue();
      Object key = entry.getKey();
      Optional<VariableLeaf> variableLeafOptional = getVariableLeaf(value);
      if (variableLeafOptional.isPresent()) {
        VariableLeaf variableLeaf = variableLeafOptional.get();
        Object resolvedValue;
        if (variableLeaf.isExpression) {
          resolvedValue = getExpressionValue(variableLeaf.getValue());
        } else {
          String[] split = ((String) value).split("\\.");
          resolvedValue = getObject(inputParams, Arrays.copyOfRange(split, 1, split.length));
        }
        if (resolvedValue != null) {
          result.put(key, resolvedValue);
        } else if (variableLeaf.isRequired()) {
          return null;
        }
      } else if (value instanceof Map) {
        Map<?, ?> children = resolveVariables((Map<?, ?>) value);
        if (children != null) {
          result.put(key, children);
        }
      }
    }
    return result.isEmpty() ? null : result;
  }

  private Object getExpressionValue(String expression) {
    return this.jexlHelper.evaluateExpression(expression, jexlContext, Object.class)
        .orElse(null);
  }

  private static Optional<VariableLeaf> getVariableLeaf(Object o) {
    String value = null;
    boolean required = false;
    boolean isExpression = false;
    if (o instanceof String) {
      value = (String) o;
    } else if (o instanceof Map<?, ?> && ((Map<?, ?>) o).keySet()
        .size() == 1 && X_DWS_EXPR.equals(((Map<?, ?>) o).keySet()
            .iterator()
            .next())) {
      value = (String) ((Map<?, ?>) o).get(X_DWS_EXPR);
      isExpression = true;
    }
    if (value != null) {
      if (value.endsWith("!")) {
        required = true;
        value = value.substring(0, value.length() - 1);
      }
      return Optional.of(VariableLeaf.builder()
          .value(value)
          .required(required)
          .isExpression(isExpression)
          .build());
    }
    return Optional.empty();

  }

  @Data
  @Builder
  private static class VariableLeaf {
    private String value;

    private boolean required;

    private boolean isExpression;
  }

  @SuppressWarnings("unchecked")
  private Object getObject(Map<String, ?> map, String[] path) {
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

  protected void initJexlContext() {
    // until params are grouped by origin, make params available under each origin
    jexlContext.set("$body", inputParams);
    jexlContext.set("$query", inputParams);
    jexlContext.set("$path", inputParams);
    jexlContext.set("$header", inputParams);
  }

}
