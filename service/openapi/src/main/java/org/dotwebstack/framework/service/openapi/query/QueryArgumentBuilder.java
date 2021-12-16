package org.dotwebstack.framework.service.openapi.query;

import static org.dotwebstack.framework.core.jexl.JexlHelper.getJexlContext;
import static org.dotwebstack.framework.service.openapi.helper.DwsExtensionHelper.getJexlExpression;
import static org.dotwebstack.framework.service.openapi.jexl.JexlUtils.evaluateJexlExpression;

import static org.dotwebstack.framework.core.datafetchers.ContextConstants.CONTEXT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.FILTER_ARGUMENT_NAME;

import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLFieldDefinition;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.jexl.JexlHelper;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
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

  public List<Argument> buildArguments(@NonNull GraphQLFieldDefinition fieldDefinition,
      @NonNull OperationRequest operationRequest) {
    var filterArguments = createFilterArguments(fieldDefinition, operationRequest);

    var contextArguments = createContextArguments(fieldDefinition, operationRequest);

    return Stream.concat(filterArguments.stream(), contextArguments.stream())
        .collect(Collectors.toList());
  }

  private List<Argument> createContextArguments(GraphQLFieldDefinition fieldDefinition,
      OperationRequest operationRequest) {
    if (fieldDefinition.getArgument(CONTEXT_ARGUMENT_NAME) == null) {
      return List.of();
    }

    var context = operationRequest.getContext()
        .getQueryProperties()
        .getContext();

    var objectField = createObjectValue(context, operationRequest.getParameters());

    return objectField != null ? List.of(new Argument(CONTEXT_ARGUMENT_NAME, objectField)) : List.of();
  }

  private List<Argument> createFilterArguments(GraphQLFieldDefinition fieldDefinition,
      OperationRequest operationRequest) {
    if (fieldDefinition.getArgument(FILTER_ARGUMENT_NAME) == null) {
      return List.of();
    }

    var filters = operationRequest.getContext()
        .getQueryProperties()
        .getFilters();

    List<ObjectField> objectFields = createObjectFields(filters, operationRequest.getParameters());

    return !objectFields.isEmpty() ? List.of(new Argument(FILTER_ARGUMENT_NAME, new ObjectValue(objectFields)))
        : List.of();
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
    var expression = map.get(OasConstants.X_DWS_EXPR);
    var jexlContext = getJexlContext(environmentProperties.getAllProperties(), parameters);
    var optionalJexlExpression = getJexlExpression(expression, map,
        expressionValue -> expressionValue.endsWith("!") ? expressionValue.substring(0, expressionValue.length() - 1)
            : expressionValue);

    var expressionResult = optionalJexlExpression
        .flatMap(jexlExpression -> evaluateJexlExpression(jexlExpression, jexlHelper, jexlContext, Object.class))
        .orElse(null);

    return expressionResult != null ? toArgumentValue(expressionResult) : null;
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
    } else if (e instanceof Boolean) {
      return new BooleanValue((Boolean) e);
    } else {
      return new StringValue(e.toString());
    }
  }

  private String paramKeyFromPath(String path) {
    return path.substring(path.lastIndexOf(".") + 1);
  }
}
