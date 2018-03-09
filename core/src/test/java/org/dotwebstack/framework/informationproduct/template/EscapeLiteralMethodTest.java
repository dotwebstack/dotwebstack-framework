package org.dotwebstack.framework.informationproduct.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import freemarker.template.SimpleScalar;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EscapeLiteralMethodTest {

  @Parameters(name = "exec_ReturnsEscapedString_Always [{1}]")
  public static List<Object[]> data() {
    // @formatter:off
    return Arrays.asList(new Object[][] {
        {"\t", "\\t"},
        {"\n", "\\n"},
        {"\r", "\\r"},
        {"\b", "\\b"},
        {"\f", "\\f"},
        {"\"", "\\\""},
        {"'", "\\'"},
        {"\\", "\\\\"},
        {"Nothing to escape here","Nothing to escape here"},
        {"Hello, I'm called \"Joes Kees\"", "Hello, I\\'m called \\\"Joes Kees\\\""}
    });
    // @formatter:on
  }

  @Parameter
  public String unescaped;

  @Parameter(1)
  public String escaped;

  @Test
  public void exec_ReturnsEscapedString_Always() throws Exception {
    // Arrange
    EscapeStringLiteralMethod escapeStringLiteralMethod = new EscapeStringLiteralMethod();

    // Act
    Object escapedString =
        escapeStringLiteralMethod.exec(Collections.singletonList(new SimpleScalar(unescaped)));

    // Assert
    assertThat(escapedString, is(escaped));
  }

}
