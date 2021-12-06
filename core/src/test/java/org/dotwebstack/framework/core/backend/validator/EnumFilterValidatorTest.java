package org.dotwebstack.framework.core.backend.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.backend.BackendExecutionStepInfo;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.config.FilterConfiguration;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnumFilterValidatorTest {

  @Mock
  private BackendExecutionStepInfo backendExecutionStepInfo;

  @Mock
  private DataFetchingEnvironment environment;

  public static Stream<Arguments> getValidEnumArguments() {
    return Stream.of(arguments(Map.of("eq", "fred")), arguments(Map.of("not", Map.of("eq", "fred"))),
        arguments(Map.of("in", List.of("waldo", "fred"))),
        arguments(Map.of("in", List.of("waldo", "fred"), "not", Map.of("in", List.of("plugh")))),
        arguments(Map.of("not", Map.of("eq", "fred"), "eq", "waldo")));
  }

  @ParameterizedTest
  @MethodSource("getValidEnumArguments")
  void validate_valid_forEnumArgument(Map<String, Object> argumentValues) {
    mockEnvironment(argumentValues);
    var schema = getSchema();

    new EnumFilterValidator(schema, backendExecutionStepInfo).validate(environment);
  }

  public static Stream<Arguments> getInvalidEnumArguments() {
    return Stream.of(arguments(Map.of("eq", "garply")), arguments(Map.of("not", Map.of("eq", "garply"))),
        arguments(Map.of("in", List.of("waldo", "garply"))),
        arguments(Map.of("in", List.of("waldo", "fred"), "not", Map.of("in", List.of("garply")))),
        arguments(Map.of("not", Map.of("eq", "fred"), "eq", "garply")));
  }

  @ParameterizedTest
  @MethodSource("getInvalidEnumArguments")
  void validate_invalid_forEnumArgument(Map<String, Object> argumentValues) {
    mockEnvironment(argumentValues);
    var schema = getSchema();

    var enumFilterValidator = new EnumFilterValidator(schema, backendExecutionStepInfo);
    var exception = assertThrows(IllegalArgumentException.class, () -> enumFilterValidator.validate(environment));

    assertThat(exception.getMessage(),
        is("Invalid filter value for filter 'BarFilter'. Valid values are: [waldo,fred,plugh]"));
  }

  private void mockEnvironment(Map<String, Object> argumentValues) {
    ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
    when(backendExecutionStepInfo.getExecutionStepInfo(any(DataFetchingEnvironment.class)))
        .thenReturn(executionStepInfo);

    Map<String, Object> values = Map.of("BarFilter", argumentValues);
    when(executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME)).thenReturn(values);

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
    var objectTypeFoo = new TestObjectType();

    var filterConfiguration = new FilterConfiguration();
    filterConfiguration.setField("bar");
    objectTypeFoo.setFilters(Map.of("BarFilter", filterConfiguration));

    var objectFieldBar = new TestObjectField();
    objectFieldBar.setType("String");
    objectFieldBar.setName("bar");

    var fieldEnumConfiguration = new FieldEnumConfiguration();
    fieldEnumConfiguration.setType("BarEnumType");
    fieldEnumConfiguration.setValues(List.of("waldo", "fred", "plugh"));
    objectFieldBar.setEnumeration(fieldEnumConfiguration);

    objectTypeFoo.setFields(Map.of("bar", objectFieldBar));
    return objectTypeFoo;
  }
}
