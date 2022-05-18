package org.dotwebstack.framework.core.backend.validator;

import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.requestValidationException;
import static org.dotwebstack.framework.core.helpers.ModelHelper.createObjectFieldPath;
import static org.dotwebstack.framework.core.helpers.ModelHelper.getObjectType;

import com.google.common.collect.Iterables;
import graphql.schema.DataFetchingEnvironment;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.backend.BackendExecutionStepInfo;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Conditional(OnLocalSchema.class)
public class FilterArgumentValidator implements GraphQlValidator {

  private final Schema schema;

  private final BackendExecutionStepInfo backendExecutionStepInfo;

  public void validate(DataFetchingEnvironment environment) {
    var executionStepInfo = backendExecutionStepInfo.getExecutionStepInfo(environment);
    Map<String, Object> filterArgument = executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME);

    var unwrappedType = TypeHelper.unwrapConnectionType(executionStepInfo.getType());
    var objectType = getObjectType(schema, unwrappedType);

    validateFilters(objectType, filterArgument);
    validateDependsOnFilters(objectType, filterArgument);
  }

  private void validateDependsOnFilters(ObjectType<?> objectType, Map<String, Object> filterArguments) {
    if (filterArguments != null && objectType.getFilters() != null) {
      filterArguments.keySet()
          .forEach(filterName -> {
            var dependsOn = Optional.ofNullable(objectType.getFilters())
                .map(map -> map.get(filterName))
                .map(FilterConfiguration::getDependsOn);

            if (dependsOn.isPresent() && !filterArguments.containsKey(dependsOn.get())) {
              throw requestValidationException("Filter value for filter '{}' depends on filter '{}'.", filterName,
                  objectType.getFilters()
                      .get(filterName)
                      .getDependsOn());
            }
          });
    }
  }

  private void validateFilters(ObjectType<?> objectType, Map<String, Object> filterArguments) {
    if (filterArguments != null) {
      filterArguments.forEach((key, value) -> validateFilter(key, objectType, value));
    }
  }

  private void validateFilter(String filterName, ObjectType<?> objectType, Object argumentValue) {
    if (FilterConstants.OR_FIELD.equalsIgnoreCase(filterName)) {
      ((Map<?, ?>) argumentValue).forEach((key, value) -> validateFilter(key.toString(), objectType, value));
    } else {
      validateFilterArgument(filterName, objectType, argumentValue);
    }
  }

  private void validateFilterArgument(String filterName, ObjectType<?> objectType, Object argumentValue) {
    if (argumentValue instanceof Map) {
      validateFilter(filterName, objectType, ((Map<?, ?>) argumentValue).entrySet());
    }
    if (argumentValue instanceof Collection) {
      ((Collection<?>) argumentValue).forEach(value -> validateFilter(filterName, objectType, value));
    }
    if (argumentValue instanceof Map.Entry) {
      var entry = ((Map.Entry<?, ?>) argumentValue);

      if (entry.getValue() instanceof Map) {
        validateFilter(filterName, objectType, entry.getValue());
      } else {
        validateFilterOperator(filterName, objectType, entry);
        validateEnumFilter(filterName, objectType, entry);
      }
    }
  }

  private void validateFilterOperator(String filterName, ObjectType<?> objectType, Map.Entry<?, ?> entry) {
    var field = getField(objectType, filterName);

    if (isNullNotAllowed(field, entry) && entry.getValue() == null) {
      throw requestValidationException("Filter value for filter '{}' for operator '{}' can't be null.", filterName,
          entry.getKey());
    }
  }

  private boolean isNullNotAllowed(ObjectField field, Map.Entry<?, ?> entry) {
    return !FilterOperator.EQ.name()
        .equalsIgnoreCase(entry.getKey()
            .toString())
        || !field.isNullable();
  }

  private void validateEnumFilter(String filterName, ObjectType<?> objectType, Map.Entry<?, ?> filterArgumentEntry) {
    var filterValues = filterArgumentEntry.getValue();
    var validValues = getValidEnumValues(objectType, filterName);

    boolean hasValidValue = Optional.of(validValues)
        .filter(not(List::isEmpty))
        .map(values -> hasValidEnumValue(filterValues, values))
        .orElse(true);

    if (!hasValidValue) {
      var validValuesAsString = getValidEnumValuesAsString(validValues);
      throw requestValidationException("Invalid filter value for filter '{}'. Valid values are: [{}]", filterName,
          validValuesAsString);
    }
  }

  private boolean hasValidEnumValue(Object argumentValue, List<Object> validValues) {
    if (argumentValue instanceof Collection) {
      return ((Collection<?>) argumentValue).stream()
          .map(value -> hasValidEnumValue(value, validValues))
          .filter(not(Boolean::booleanValue))
          .findFirst()
          .orElse(true);
    }
    return argumentValue == null || validValues.contains(argumentValue);
  }

  private String getValidEnumValuesAsString(List<Object> validValues) {
    return validValues.stream()
        .map(Object::toString)
        .collect(Collectors.joining(","));
  }

  private List<Object> getValidEnumValues(ObjectType<?> objectType, String filterName) {
    return Optional.ofNullable(getField(objectType, filterName).getEnumeration())
        .map(FieldEnumConfiguration::getValues)
        .orElse(List.of());
  }

  private ObjectField getField(ObjectType<?> objectType, String filterName) {
    return Optional.of(objectType.getFilters())
        .map(filters -> filters.get(filterName))
        .map(FilterConfiguration::getField)
        .map(filterFieldPath -> createObjectFieldPath(schema, objectType, filterFieldPath))
        .map(Iterables::getLast)
        .orElseThrow(() -> requestValidationException("No corresponding field found for Filter name {}.", filterName));
  }
}
