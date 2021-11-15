package org.dotwebstack.framework.service.openapi.query;

import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;

public class QueryArgumentUtil {

  private QueryArgumentUtil() {}

  public static List<Argument> createArguments(OperationRequest operationRequest) {
    List<Argument> results = createKeyArguments(operationRequest);
    results.addAll(createFilterArguments(operationRequest));
    return results;
  }

  private static List<Argument> createKeyArguments(OperationRequest operationRequest) {
    Map<String, Object> paramKeyValues = operationRequest.getContext()
        .getQueryProperties()
        .getKeys()
        .entrySet()
        .stream()
        .filter(e -> operationRequest.getParameters()
            .containsKey(paramKeyFromPath(e.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, e -> operationRequest.getParameters()
            .get(paramKeyFromPath(e.getValue()))));

    return paramKeyValues.entrySet()
        .stream()
        .map(e -> new Argument(e.getKey(), toArgumentValue(e.getValue())))
        .collect(Collectors.toList());
  }

  private static List<Argument> createFilterArguments(OperationRequest operationRequest) {
    Map<String, Map<String, Object>> filters = operationRequest.getContext()
        .getQueryProperties()
        .getFilters();
    List<ObjectField> objectFields = createObjectFields(filters, operationRequest.getParameters());

    return !objectFields.isEmpty() ? List.of(new Argument("filter", new ObjectValue(objectFields))) : List.of();
  }

  private static List<ObjectField> createObjectFields(Map<String, Map<String, Object>> map,
      Map<String, Object> parameters) {
    return map.entrySet()
        .stream()
        .map(entry -> {
          String key = entry.getKey();
          Value<?> value = createValue(entry.getValue(), parameters);
          return value != null ? new ObjectField(key, value) : null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @SuppressWarnings({"unchecked"})
  private static List<ObjectField> createObjectField(Map<String, Object> map, Map<String, Object> parameters) {
    return map.entrySet()
        .stream()
        .map(entry -> {
          String key = entry.getKey();
          Object value = entry.getValue();
          if (value instanceof Map) {
            Value<?> objectValue = createValue((Map<String, Object>) value, parameters);
            return objectValue != null ? new ObjectField(key, objectValue) : null;
          } else if (value instanceof String) {
            String path = (String) value;
            String paramKey = paramKeyFromPath(path);
            Object paramValue = parameters.get(paramKey);
            return paramValue != null ? new ObjectField(key, toArgumentValue(paramValue)) : null;
          } else {
            throw new IllegalArgumentException("Type not supported: " + value.getClass()
                .getSimpleName());
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @SuppressWarnings({"unchecked"})
  private static Value<?> createValue(Map<String, Object> filter, Map<String, Object> parameters) {
    List<ObjectField> objectFields = filter.entrySet()
        .stream()
        .map(e -> {
          String key = e.getKey();
          Object value = e.getValue();

          if (value instanceof String) {
            String path = (String) value;
            String paramKey = paramKeyFromPath(path);
            Object paramValue = parameters.get(paramKey);
            return paramValue != null ? new ObjectField(key, toArgumentValue(paramValue)) : null;
          } else if (value instanceof Map) {
            List<ObjectField> childFields = createObjectField((Map<String, Object>) value, parameters);
            return childFields.isEmpty() ? null : new ObjectField(key, new ObjectValue(childFields));
          } else {
            throw new IllegalArgumentException("Type not supported: " + value.getClass()
                .getSimpleName());
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    return objectFields.isEmpty() ? null : new ObjectValue(objectFields);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Value<?> toArgumentValue(Object e) {
    if (e instanceof String) {
      return new StringValue((String) e);
    } else if (e instanceof List) {
      List<Value> values = ((List<Object>) e).stream()
          .map(QueryArgumentUtil::toArgumentValue)
          .collect(Collectors.toList());
      return new ArrayValue(values);
    } else {
      // TODO: support other types
      return new StringValue(e.toString());
    }
  }

  private static String paramKeyFromPath(String path) {
    return path.substring(path.lastIndexOf(".") + 1);
  }
}
