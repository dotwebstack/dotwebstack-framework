package org.dotwebstack.framework;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;


@RunWith(MockitoJUnitRunner.class)
public class EnvironmentAwareResourceTest {

  @Mock
  private Environment environment;

  @Before
  public void setUp() throws Exception {
    when(environment.getProperty("NAME")).thenReturn(DBEERPEDIA.BREWERY_DAVO_NAME);
    when(environment.getProperty("ENVIRONMENT_VARIABLE_IS_LONGER_THAN_VALUE")).thenReturn(
        DBEERPEDIA.BREWERY_DAVO_NAME);
  }

  @Test
  public void getInputStream_VariableIsReplaced_WithBrackets() throws IOException {
    // Arrange
    String input = "${NAME}";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output =
        getString(new EnvironmentAwareResource(inputStream, environment).getInputStream());

    // Assert
    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  @Test
  public void getInputStream_VariableIsReplaced_WithoutBrackets() throws IOException {
    // Arrange
    String input = "$NAME";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output =
        getString(new EnvironmentAwareResource(inputStream, environment).getInputStream());

    // Assert
    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  @Test
  public void getInputStream_VariablAreIsReplaced_WithMultipleValues() throws IOException {
    // Arrange
    String input = "$NAME${NAME}$NAME";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output =
        getString(new EnvironmentAwareResource(inputStream, environment).getInputStream());

    // Assert
    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME + DBEERPEDIA.BREWERY_DAVO_NAME
        + DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  @Test
  public void getInputStream_VariableIsReplaced_WithSpecialChars() throws IOException {
    // Arrange
    String input = "$$NAME:${NAME}-$NAME$$NAME4$NAME";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output =
        getString(new EnvironmentAwareResource(inputStream, environment).getInputStream());

    // Assert
    assertThat(output,
        is(String.format("$%s:%s-%s$%s4%s", DBEERPEDIA.BREWERY_DAVO_NAME,
            DBEERPEDIA.BREWERY_DAVO_NAME, DBEERPEDIA.BREWERY_DAVO_NAME,
            DBEERPEDIA.BREWERY_DAVO_NAME, DBEERPEDIA.BREWERY_DAVO_NAME)));
  }

  @Test
  public void getInputStream_VariableIsReplaced_WithNewLines() throws IOException {
    // Arrange
    String input = "$NAME\n${NAME}\n\r$NAME\r";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output =
        getString(new EnvironmentAwareResource(inputStream, environment).getInputStream());

    // Assert
    assertThat(output, is(String.format("%s\n%s\n\r%s\r", DBEERPEDIA.BREWERY_DAVO_NAME,
        DBEERPEDIA.BREWERY_DAVO_NAME, DBEERPEDIA.BREWERY_DAVO_NAME)));
  }

  @Test
  public void getInputStream_VariableNotReplaced_WithFollowedByLetter() throws IOException {
    // Arrange
    String input = "$NAMES";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output =
        getString(new EnvironmentAwareResource(inputStream, environment).getInputStream());

    // Assert
    assertThat(output, is(input));
  }

  @Test
  public void getInputStream_VariableNotReplaced_WithFollowedByUnderscore() throws IOException {
    // Arrange
    String input = "$NAME_";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output =
        getString(new EnvironmentAwareResource(inputStream, environment).getInputStream());

    // Assert
    assertThat(output, is(input));
  }

  @Test
  public void getInputStream_VariableIsReplaced_LongName() throws IOException {
    // Arrange
    String input = "$ENVIRONMENT_VARIABLE_IS_LONGER_THAN_VALUE";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output =
        getString(new EnvironmentAwareResource(inputStream, environment).getInputStream());

    // Assert
    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  private String getString(InputStream stream) throws IOException {
    return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
  }
}
