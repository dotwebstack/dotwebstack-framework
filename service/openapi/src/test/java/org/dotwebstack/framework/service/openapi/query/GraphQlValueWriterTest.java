package org.dotwebstack.framework.service.openapi.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GraphQlValueWriterTest {

  @ParameterizedTest(name = "GraphQlWriter serializes {1}")
  @MethodSource("valueWriterArguments")
  void write_returnsExpectedResult(Object input, String expectedResult) {
    StringBuilder sb = new StringBuilder();
    GraphQlValueWriter.write(input, sb);

    assertEquals(expectedResult, sb.toString());
  }

  @Test
  void write_throwsException_forUnsupportedClass() {
    Object value = new Object();
    StringBuilder sb = new StringBuilder();

    assertThrows(IllegalArgumentException.class, () -> GraphQlValueWriter.write(value, sb));
  }

  private static Stream<Arguments> valueWriterArguments() {
    return Stream.of(arguments(1, "1"), arguments("1", "\"1\""),
        arguments(List.of("value", 1.3f, 2.0d), "[\"value\", 1.3, 2.0]"), arguments(
            new TreeMap<>(Map.of("key", "value", "list", List.of(1, 2, 3))), "{key: \"value\", list: [1, 2, 3]}"));
  }
}
