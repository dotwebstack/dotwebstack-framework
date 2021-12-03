package org.dotwebstack.framework.core.backend.validator;

import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ModelHelper.createObjectFieldPath;
import static org.dotwebstack.framework.core.helpers.ModelHelper.getObjectType;

import com.google.common.collect.Iterables;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.dotwebstack.framework.core.backend.BackendExecutionStepInfo;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EnumFilterValidator implements GraphQlValidator {

  private final Schema schema;

  private final BackendExecutionStepInfo backendExecutionStepInfo;

  public void validate(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo = backendExecutionStepInfo.getExecutionStepInfo(environment);
    Map<String, Object> filterArgument = executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME);

    var unwrappedType = TypeHelper.unwrapConnectionType(executionStepInfo.getType());
    var objectType = getObjectType(schema, unwrappedType);

    validateFilters(objectType, filterArgument);
  }

  private void validateFilters(ObjectType<?> objectType, Map<String, Object> filterArguments) {
    if (filterArguments != null) {
      filterArguments.entrySet()
          .forEach(filterArgumentEntry -> validateFilter(objectType, filterArgumentEntry));
    }
  }

  private void validateFilter(ObjectType<?> objectType, Map.Entry<String, Object> filterArgumentEntry) {
    String filterName = filterArgumentEntry.getKey();
    Object filterValues = filterArgumentEntry.getValue();

    List<Object> validValues = getValidValues(objectType, filterName);

    boolean hasValidValue = Optional.of(validValues)
        .filter(not(List::isEmpty))
        .map(values -> hasValidValue(filterValues, values))
        .orElse(true);

    if (!hasValidValue) {
      String validValuesAsString = getValidValuesAsString(validValues);
      throw illegalArgumentException(String.format("Invalid filter value for filter '%s'. Valid values are: [%s]",
          filterName, validValuesAsString));
    }
  }

  private String getValidValuesAsString(List<Object> validValues) {
    return validValues.stream()
        .map(Object::toString)
        .collect(Collectors.joining(","));
  }

  private List<Object> getValidValues(ObjectType<?> objectType, String filterName) {
    return Optional.of(objectType.getFilters())
        .map(filters -> filters.get(filterName))
        .map(FilterConfiguration::getField)
        .map(filterFieldPath -> createObjectFieldPath(schema, objectType, filterFieldPath))
        .map(Iterables::getLast)
        .map(ObjectField::getEnumeration)
        .map(FieldEnumConfiguration::getValues)
        .orElse(List.of());
  }

  private boolean hasValidValue(Object argumentValue, List<Object> validValues) {
    if (argumentValue instanceof Map) {
      return hasValidValue(Iterables.getFirst(((Map<?, ?>) argumentValue).values(), null), validValues);
    }
    if (argumentValue instanceof Collection) {
      return ((Collection<?>) argumentValue).stream()
          .map(value -> hasValidValue(value, validValues))
          .filter(not(Boolean::booleanValue))
          .findFirst()
          .orElse(true);
    }
    return validValues.contains(argumentValue);
  }

}
