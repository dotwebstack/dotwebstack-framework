package org.dotwebstack.framework.service.openapi.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.AstPrinter;
import graphql.language.BooleanValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.jexl3.JexlBuilder;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.EnvironmentProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryArgumentBuilderTest {

  private QueryArgumentBuilder queryArgumentBuilder;

  @Mock
  private EnvironmentProperties environmentProperties;

  @BeforeEach
  void beforeAll() {
    queryArgumentBuilder = new QueryArgumentBuilder(environmentProperties, new JexlBuilder().silent(false)
        .strict(true)
        .create());
  }

  @SuppressWarnings("unchecked")
  @Test
  void buildArguments_returnsExpectedArgument() throws IOException {
    Map<String, Map<String, Object>> filter = (Map<String, Map<String, Object>>) TestResources.filter("filter1");
    OperationRequest operationRequest = createOperationRequest(filter);

    Map<String, Object> parameters = Map.of("p1", "value1", "p2", "value2", "p3", "value3", "p4", "value4");
    when(operationRequest.getParameters()).thenReturn(parameters);

    List<Argument> arguments = queryArgumentBuilder.buildArguments(operationRequest);
    assertThat(arguments.size(), is(1));
    String pretty = AstPrinter.printAst(arguments.get(0));

    assertThat(pretty, is("filter: {filter : {field1 : \"value1\", field2 : {and : {field3 : \"value2\", field4 : "
        + "\"value3_suffix\", field5 : {not : {field6 : \"value4\"}}}}}}"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void buildArguments_throwsExcepction_forUnsupportedType() throws IOException {
    Map<String, Map<String, Object>> filter =
        (Map<String, Map<String, Object>>) TestResources.filter("filter_unsupported_type");
    OperationRequest operationRequest = createOperationRequest(filter);

    assertThrows(IllegalArgumentException.class, () -> queryArgumentBuilder.buildArguments(operationRequest));
  }

  @SuppressWarnings("unchecked")
  @Test
  void buildArguments_throwsExcepction_forUnsupportedTypeAtRoot() throws IOException {
    Map<String, Map<String, Object>> filter =
        (Map<String, Map<String, Object>>) TestResources.filter("filter_unsupported_type_root");
    OperationRequest operationRequest = createOperationRequest(filter);

    assertThrows(IllegalArgumentException.class, () -> queryArgumentBuilder.buildArguments(operationRequest));
  }

  private OperationRequest createOperationRequest(Map<String, Map<String, Object>> filter) {
    OperationRequest operationRequest = mock(OperationRequest.class, Answers.RETURNS_DEEP_STUBS);
    when(operationRequest.getContext()
        .getQueryProperties()
        .getFilters()).thenReturn(filter);
    return operationRequest;
  }

  @ParameterizedTest
  @MethodSource("argumentObjects")
  void toArgumentValue_returnsExpectedValue(Object input, Value<?> expectedValue) {
    Value<?> value = queryArgumentBuilder.toArgumentValue(input);

    assertTrue(value.isEqualTo(expectedValue));
  }

  private static Stream<Arguments> argumentObjects() {
    return Stream.of(Arguments.of("string", new StringValue("string")),
        Arguments.of(List.of("1"), new ArrayValue(List.of(new StringValue("1")))),
        Arguments.of(1, new IntValue(new BigInteger("1"))), Arguments.of(1L, new IntValue(new BigInteger("1"))),
        Arguments.of(1.2f, new FloatValue(new BigDecimal("1.2"))),
        Arguments.of(1.2d, new FloatValue(new BigDecimal("1.2"))),
        Arguments.of(Map.of("1", "2"), new StringValue("{1=2}")), Arguments.of(false, new BooleanValue(false)));
  }
}
