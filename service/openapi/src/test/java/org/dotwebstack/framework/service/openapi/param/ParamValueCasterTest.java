package org.dotwebstack.framework.service.openapi.param;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.media.Schema;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class ParamValueCasterTest {


  @ParameterizedTest
  @MethodSource("castScalarArguments")
  void castValue(Object value, Schema<?> schema, Object expectedResult) {
    Object result = ParamValueCaster.cast(value, schema);

    assertThat(result, is(expectedResult));
  }


  private static Stream<Arguments> castScalarArguments() {
    return Stream.of(Arguments.of("string", mockSchema("string", null), "string"),
        Arguments.of("1.4", mockSchema("number", "float"), 1.4f),
        Arguments.of("1.4", mockSchema("number", "double"), 1.4d),
        Arguments.of("1.4", mockSchema("number", null), new BigDecimal("1.4")),
        Arguments.of("14", mockSchema("integer", "int64"), 14L), Arguments.of("14", mockSchema("integer", "int32"), 14),
        Arguments.of("14", mockSchema("integer", null), new BigInteger("14")),
        Arguments.of("true", mockSchema("boolean", null), true),
        Arguments.of("false", mockSchema("boolean", null), false));
  }

  private static Schema<?> mockSchema(String type, String format) {
    Schema<?> result = mock(Schema.class);
    when(result.getType()).thenReturn(type);
    when(result.getFormat()).thenReturn(format);
    return result;
  }


}
