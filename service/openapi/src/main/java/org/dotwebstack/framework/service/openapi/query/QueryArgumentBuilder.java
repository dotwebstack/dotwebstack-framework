package org.dotwebstack.framework.service.openapi.query;

import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.dotwebstack.framework.service.openapi.jexl.JexlContextUtils;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QueryArgumentBuilder {

  private final EnvironmentProperties environmentProperties;

  private final JexlHelper jexlHelper;

  public QueryArgumentBuilder(@NonNull EnvironmentProperties environmentProperties, @NonNull JexlEngine jexlEngine) {
    this.environmentProperties = environmentProperties;
    this.jexlHelper = new JexlHelper(jexlEngine);
  }

  public List<Argument> buildArguments(@NonNull OperationRequest operationRequest) {
    return createFilterArguments(operationRequest);
  }

  private List<Argument> createFilterArguments(OperationRequest operationRequest) {
    var filters = operationRequest.getContext()
        .getQueryProperties()
        .getFilters();
    List<ObjectField> objectFields = createObjectFields(filters, operationRequest.getParameters());

    return !objectFields.isEmpty() ? List.of(new Argument("filter", new ObjectValue(objectFields))) : List.of();
  }

  private List<ObjectField> createObjectFields(Map<String, Map<String, Object>> map, Map<String, Object> parameters) {
    return map.entrySet()
        .stream()
        .map(entry -> {
          var key = entry.getKey();
          var value = createObjectValue(entry.getValue(), parameters);
          return value != null ? new ObjectField(key, value) : null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @SuppressWarnings({"unchecked"})
  private List<ObjectField> createObjectField(Map<String, Object> map, Map<String, Object> parameters) {
    return map.entrySet()
        .stream()
        .map(entry -> {
          var key = entry.getKey();
          var value = entry.getValue();
          if (value instanceof Map && isExpression((Map<String, Object>) value)) {
            var objectValue = createExpressionObjectValue((Map<String, Object>) value, parameters);
            return objectValue != null ? new ObjectField(key, objectValue) : null;
          } else if (value instanceof Map) {
            var objectValue = createObjectValue((Map<String, Object>) value, parameters);
            return objectValue != null ? new ObjectField(key, objectValue) : null;
          } else if (value instanceof String) {
            return filterValueToObjectField(key, (String) value, parameters);
          } else {
            throw new IllegalArgumentException("Type not supported: " + value.getClass()
                .getSimpleName());
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @SuppressWarnings({"unchecked"})
  private ObjectValue createObjectValue(Map<String, Object> map, Map<String, Object> parameters) {
    List<ObjectField> objectFields = map.entrySet()
        .stream()
        .map(e -> {
          var key = e.getKey();
          var value = e.getValue();

          if (value instanceof String) {
            return filterValueToObjectField(key, (String) value, parameters);
          } else if (value instanceof Map && isExpression((Map<String, Object>) value)) {
            var objectValue = createExpressionObjectValue((Map<String, Object>) value, parameters);
            return objectValue != null ? new ObjectField(key, objectValue) : null;
          } else if (value instanceof Map) {
            var childFields = createObjectField((Map<String, Object>) value, parameters);
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

  private ObjectField filterValueToObjectField(String key, String value, Map<String, Object> parameters) {
    var paramKey = paramKeyFromPath(value);
    var paramValue = parameters.get(paramKey);
    return paramValue != null ? new ObjectField(key, toArgumentValue(paramValue)) : null;
  }

  private boolean isExpression(Map<String, Object> map) {
    return map.size() == 1 && map.containsKey(OasConstants.X_DWS_EXPR);
  }

  @SuppressWarnings({"rawtypes"})
  protected Value createExpressionObjectValue(Map<String, Object> map, Map<String, Object> parameters) {
    var expression = (String) map.get(OasConstants.X_DWS_EXPR);
    if (expression.endsWith("!")) {
      expression = expression.substring(0, expression.length() - 1);
    }
    var jexlContext = JexlContextUtils.createJexlContext(environmentProperties, parameters);
    var expressionValue = this.jexlHelper.evaluateExpression(expression, jexlContext, Object.class)
        .orElse(null);
    return expressionValue != null ? toArgumentValue(expressionValue) : null;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  protected Value toArgumentValue(Object e) {
    if (e instanceof String) {
      return new StringValue((String) e);
    } else if (e instanceof List) {
      List<Value> values = ((List<Object>) e).stream()
          .map(this::toArgumentValue)
          .collect(Collectors.toList());
      return new ArrayValue(values);
    } else if (e instanceof Integer || e instanceof Long) {
      return new IntValue(new BigInteger(e.toString()));
    } else if (e instanceof Float || e instanceof Double) {
      return new FloatValue(new BigDecimal(e.toString()));
    } else {
      return new StringValue(e.toString());
    }
  }

  private String paramKeyFromPath(String path) {
    return path.substring(path.lastIndexOf(".") + 1);
  }
}
