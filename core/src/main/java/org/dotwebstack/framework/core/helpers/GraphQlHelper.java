package org.dotwebstack.framework.core.helpers;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.isObjectType;
import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_CONNECTION_TYPE;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_SCALAR;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.KEY_FIELD;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import graphql.execution.ExecutionStepInfo;
import graphql.language.BooleanValue;
import graphql.language.FieldDefinition;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.Node;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.language.Value;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.NonNull;

public class GraphQlHelper {

  private static final String QUERY_TYPE_NAME = "Query";

  private GraphQlHelper() {}

  public static Object getValue(@NonNull Type<?> type, @NonNull Value<?> value) {
    var stringValue = getStringValue(value);

    if ((type instanceof TypeName) && Objects.equals("Date", ((TypeName) type).getName())
        && Objects.equals("NOW", stringValue)) {
      return LocalDate.now();
    }
    return stringValue;
  }

  public static String getStringValue(@NonNull Value<?> value) {
    if (value instanceof IntValue) {
      return ((IntValue) value).getValue()
          .toString();
    } else if (value instanceof StringValue) {
      return ((StringValue) value).getValue();
    } else if (value instanceof FloatValue) {
      return ((FloatValue) value).getValue()
          .toString();
    } else if (value instanceof BooleanValue) {
      return Boolean.toString(((BooleanValue) value).isValue());
    }
    return value.toString();
  }

  public static final Predicate<SelectedField> isScalarField = selectedField -> {
    var unwrappedType = unwrapAll(selectedField.getType());

    return (unwrappedType instanceof GraphQLScalarType || unwrappedType instanceof GraphQLEnumType
        || isScalarType(unwrappedType));
  };

  public static final Predicate<SelectedField> isObjectField = selectedField -> {
    var unwrappedType = unwrapAll(selectedField.getType());
    var additionalData = getAdditionalData(unwrappedType);

    return !isList(unwrapNonNull(selectedField.getType())) && isObjectType(unwrappedType)
        && !isScalarType(unwrappedType) && !additionalData.containsKey(IS_CONNECTION_TYPE) && !unwrappedType.getName()
            .equals(AGGREGATE_TYPE);
  };

  public static final Predicate<SelectedField> isObjectListField = selectedField -> {
    var unwrappedType = unwrapAll(selectedField.getType());

    return (isList(unwrapNonNull(selectedField.getType())) && isObjectType(unwrapAll(selectedField.getType())))
        && !unwrappedType.getName()
            .equals(AGGREGATE_TYPE)
        || getAdditionalData(unwrappedType).containsKey(IS_CONNECTION_TYPE);
  };

  public static final Predicate<SelectedField> isCustomValueField = selectedField -> selectedField.getFieldDefinitions()
      .stream()
      .anyMatch(fieldDefinition -> requireNonNull(fieldDefinition.getDefinition()).getAdditionalData()
          .containsKey(CUSTOM_FIELD_VALUEFETCHER));

  public static final Predicate<SelectedField> isIntrospectionField = selectedField -> selectedField.getName()
      .startsWith("__");

  private static boolean isScalarType(GraphQLUnmodifiedType unmodifiedType) {
    return getAdditionalData(unmodifiedType).containsKey(IS_SCALAR);
  }

  public static ExecutionStepInfo getRequestStepInfo(ExecutionStepInfo executionStepInfo) {
    if (executionStepInfo.hasParent() && executionStepInfo.getParent()
        .hasParent()) {
      return getRequestStepInfo(executionStepInfo.getParent());
    }
    return executionStepInfo;
  }

  public static List<GraphQLArgument> getKeyArguments(GraphQLFieldDefinition fieldDefinition) {
    return fieldDefinition.getArguments()
        .stream()
        .filter(argument -> requireNonNull(argument.getDefinition()).getAdditionalData()
            .containsKey(KEY_FIELD))
        .collect(Collectors.toList());
  }

  public static FieldDefinition getFieldDefinition(SelectedField selectedField) {
    if (selectedField.getFieldDefinitions()
        .size() > 1) {
      throw illegalArgumentException("SelectedField '{}' has {} fieldDefinitions but expected one!",
          selectedField.getName(), selectedField.getFieldDefinitions()
              .size());
    }
    return selectedField.getFieldDefinitions()
        .stream()
        .findFirst()
        .orElseThrow()
        .getDefinition();
  }

  @SuppressWarnings({"unchecked"})
  private static Map<String, String> getAdditionalData(GraphQLUnmodifiedType unmodifiedType) {
    return ofNullable(unmodifiedType).map(GraphQLUnmodifiedType::getDefinition)
        .map(Node::getAdditionalData)
        .orElse(Map.of());
  }

  public static Optional<String> getAdditionalData(SelectedField selectedField, String key) {
    var fieldDefinition = getFieldDefinition(selectedField);

    return getAdditionalData(fieldDefinition, key);
  }

  public static Optional<String> getAdditionalData(FieldDefinition fieldDefinition, String key) {
    return Optional.of(fieldDefinition)
        .filter(def -> def.getAdditionalData()
            .containsKey(key))
        .map(def -> def.getAdditionalData()
            .get(key));
  }

  public static Optional<String> getQueryName(ExecutionStepInfo executionStepInfo) {
    return Optional.of(executionStepInfo)
        .map(GraphQlHelper::getRequestStepInfo)
        .filter(requestStepInfo -> requestStepInfo.getObjectType()
            .getName()
            .equals(QUERY_TYPE_NAME))
        .map(ExecutionStepInfo::getFieldDefinition)
        .map(GraphQLFieldDefinition::getName);
  }
}
