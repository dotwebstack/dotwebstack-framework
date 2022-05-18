package org.dotwebstack.framework.core.backend.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.util.StringUtils.capitalize;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.RequestValidationException;
import org.dotwebstack.framework.core.backend.BackendExecutionStepInfo;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterArgumentValidatorTest {

  @Mock
  private BackendExecutionStepInfo backendExecutionStepInfo;

  @Mock
  private DataFetchingEnvironment environment;

  public static Stream<Arguments> getValidEnumArguments() {
    return Stream.of(arguments(Map.of("BarFilter", Map.of("eq", "fred"))),
        arguments(Map.of("BarFilter", Map.of("not", Map.of("eq", "fred")))),
        arguments(Map.of("BarFilter", Map.of("in", List.of("waldo", "fred")))),
        arguments(Map.of("BarFilter", Map.of("in", List.of("waldo", "fred"), "not", Map.of("in", List.of("plugh"))))),
        arguments(Map.of("BarFilter", Map.of("not", Map.of("eq", "fred"), "eq", "waldo"))),
        arguments(Map.of("_or", Map.of("BarFilter", Map.of("eq", "fred"), "eq", "waldo"))));
  }

  @ParameterizedTest
  @MethodSource("getValidEnumArguments")
  void validate_valid_forEnumArgument(Map<String, Object> argumentValues) {
    mockEnvironment(argumentValues);
    var schema = getSchema();

    assertDoesNotThrow(() -> new FilterArgumentValidator(schema, backendExecutionStepInfo).validate(environment));
  }

  public static Stream<Arguments> getInvalidEnumArguments() {
    return Stream.of(arguments(Map.of("BarFilter", Map.of("eq", "garply"))),
        arguments(Map.of("BarFilter", Map.of("not", Map.of("eq", "garply")))),
        arguments(Map.of("BarFilter", Map.of("in", List.of("waldo", "garply")))),
        arguments(Map.of("BarFilter", Map.of("in", List.of("waldo", "fred"), "not", Map.of("in", List.of("garply"))))),
        arguments(Map.of("BarFilter", Map.of("not", Map.of("eq", "fred"), "eq", "garply"))),
        arguments(Map.of("_or", Map.of("BarFilter", Map.of("eq", "garply"), "eq", "fred"))));
  }

  @ParameterizedTest
  @MethodSource("getInvalidEnumArguments")
  void validate_invalid_forEnumArgument(Map<String, Object> argumentValues) {
    mockEnvironment(argumentValues);
    var schema = getSchema();

    var filterArgumentValidator = new FilterArgumentValidator(schema, backendExecutionStepInfo);
    var exception = assertThrows(RequestValidationException.class, () -> filterArgumentValidator.validate(environment));

    assertThat(exception.getMessage(),
        is("Invalid filter value for filter 'BarFilter'. Valid values are: [waldo,fred,plugh]"));
  }

  public static Stream<Arguments> getValidOperatorArguments() {
    return Stream.of(arguments(map("ThudFilter", map("eq", "fred"))),
        arguments(map("BazFilter", map("not", map("eq", null)))),
        arguments(map("BazFilter", map("in", List.of("fred")))), arguments(map("BarFilter", map("eq", null))),
        arguments(map("_or", map("ThudFilter", map("eq", "Fred")))),
        arguments(map("_or", map("BazFilter", map("eq", null)))));
  }

  @ParameterizedTest
  @MethodSource("getValidOperatorArguments")
  void validate_valid_forOperatorArgument(Map<String, Object> argumentValues) {
    mockEnvironment(argumentValues);
    var schema = getSchema();

    assertDoesNotThrow(() -> new FilterArgumentValidator(schema, backendExecutionStepInfo).validate(environment));
  }

  public static Stream<Arguments> getInvalidOperatorArguments() {
    return Stream.of(arguments(map("ThudFilter", map("eq", null)), "eq"),
        arguments(map("ThudFilter", map("not", map("eq", null))), "eq"),
        arguments(map("ThudFilter", map("in", null)), "in"), arguments(map("ThudFilter", map("lt", null)), "lt"),
        arguments(map("_or", map("ThudFilter", map("eq", null))), "eq"));
  }

  @ParameterizedTest
  @MethodSource("getInvalidOperatorArguments")
  void validate_invalid_forOperatorArgument(Map<String, Object> argumentValues, String expectedOperatorInException) {
    mockEnvironment(argumentValues);
    var schema = getSchema();

    var filterArgumentValidator = new FilterArgumentValidator(schema, backendExecutionStepInfo);
    var exception = assertThrows(RequestValidationException.class, () -> filterArgumentValidator.validate(environment));

    assertThat(exception.getMessage(), is(String
        .format("Filter value for filter 'ThudFilter' for operator '%s' can't be null.", expectedOperatorInException)));
  }

  @Test
  void validate_valid_forDependsOnFilter() {
    Map<String, Object> arguments =
        Map.of("QuuxFilter", map("eq", "Fred"), "QuuzFilter", map("eq", "Fred"), "QuxFilter", map("eq", "Fred"));
    mockEnvironment(arguments);
    var schema = getSchema();

    assertDoesNotThrow(() -> new FilterArgumentValidator(schema, backendExecutionStepInfo).validate(environment));
  }

  public static Stream<Arguments> getMissingDependsOnFilters() {
    return Stream.of(arguments(new String[] {"QuuxFilter"}, "QuxFilter"),
        arguments(new String[] {"QuuzFilter"}, "QuuxFilter"),
        arguments(new String[] {"QuuxFilter", "QuuzFilter"}, "QuxFilter"));
  }

  @ParameterizedTest
  @MethodSource("getMissingDependsOnFilters")
  void validate_invalid_forMissingDependsOnFilter(String[] filterArguments, String expectedDependsOnException) {
    Map<String, Object> arguments = Arrays.stream(filterArguments)
        .collect(Collectors.toMap(x -> x, x -> map("eq", "Fred")));
    mockEnvironment(arguments);
    var schema = getSchema();

    var filterArgumentValidator = new FilterArgumentValidator(schema, backendExecutionStepInfo);
    var exception = assertThrows(RequestValidationException.class, () -> filterArgumentValidator.validate(environment));

    assertThat(exception.getMessage(), is(String.format("Filter value for filter '%s' depends on filter '%s'.",
        filterArguments[0], expectedDependsOnException)));
  }

  private static Map<String, Object> map(String key, Object value) {
    Map<String, Object> map = new HashMap<>();
    map.put(key, value);
    return map;
  }

  private void mockEnvironment(Map<String, Object> argumentValues) {
    ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
    when(backendExecutionStepInfo.getExecutionStepInfo(any(DataFetchingEnvironment.class)))
        .thenReturn(executionStepInfo);

    when(executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME)).thenReturn(argumentValues);

    var objectType = GraphQLObjectType.newObject()
        .name("Foo")
        .build();
    when(executionStepInfo.getType()).thenReturn(objectType);
  }

  private Schema getSchema() {
    var schema = new Schema();
    schema.setObjectTypes(Map.of("Foo", createFooObjectType()));
    return schema;
  }

  private TestObjectType createFooObjectType() {
    FilterConfiguration filterConfigurationBar = createFilterConfiguration("bar", null);
    FilterConfiguration filterConfigurationBaz = createFilterConfiguration("baz", null);
    FilterConfiguration filterConfigurationThud = createFilterConfiguration("thud", null);
    FilterConfiguration filterConfigurationQux = createFilterConfiguration("qux", null);
    FilterConfiguration filterConfigurationQuux = createFilterConfiguration("quux", "QuxFilter");
    FilterConfiguration filterConfigurationQuuz = createFilterConfiguration("quuz", "QuuxFilter");

    var objectTypeFoo = new TestObjectType();
    objectTypeFoo.setFilters(Map.of("BarFilter", filterConfigurationBar, "BazFilter", filterConfigurationBaz,
        "ThudFilter", filterConfigurationThud, "QuxFilter", filterConfigurationQux, "QuuxFilter",
        filterConfigurationQuux, "QuuzFilter", filterConfigurationQuuz));

    var objectFieldBar = createObjectField("String", "bar");
    objectFieldBar.setNullable(true);

    var fieldEnumConfiguration = new FieldEnumConfiguration();
    fieldEnumConfiguration.setType("BarEnumType");
    fieldEnumConfiguration.setValues(List.of("waldo", "fred", "plugh"));
    objectFieldBar.setEnumeration(fieldEnumConfiguration);

    var objectFieldBaz = createObjectField("String", "baz");
    objectFieldBaz.setNullable(true);

    var objectFieldThud = createObjectField("int", "thud");
    var objectFieldQux = createObjectField("String", "qux");
    var objectFieldQuux = createObjectField("String", "quux");
    var objectFieldQuuz = createObjectField("String", "quuz");

    objectTypeFoo.setFields(Map.of("bar", objectFieldBar, "baz", objectFieldBaz, "thud", objectFieldThud, "qux",
        objectFieldQux, "quux", objectFieldQuux, "quuz", objectFieldQuuz));
    return objectTypeFoo;
  }

  private FilterConfiguration createFilterConfiguration(String name, String dependsOn) {
    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setName(String.format("%sFilter", capitalize(name)));
    filterConfiguration.setField(name);
    filterConfiguration.setDependsOn(dependsOn);
    return filterConfiguration;
  }

  private TestObjectField createObjectField(String type, String name) {
    var objectField = new TestObjectField();
    objectField.setType(type);
    objectField.setName(name);
    return objectField;
  }
}
