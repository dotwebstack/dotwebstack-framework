package org.dotwebstack.framework;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({EnvironmentAwareResource.class})
public class EnvironmentAwareResourceTest {

  @Before
  public void setUp() throws Exception {
    Map<String, String> env = new HashMap<>();
    env.put("NAME", DBEERPEDIA.BREWERY_DAVO_NAME);
    env.put("ENVIRONMENT_VARIABLE_IS_LONGER_THAN_VALUE", DBEERPEDIA.BREWERY_DAVO_NAME);

    PowerMockito.mockStatic(System.class);
    when(System.getenv()).thenReturn(env);
  }

  @Test
  public void getInputStream_VariableIsReplaced_WithBrackets() throws IOException {
    // Arrange
    String input = "${NAME}";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output = IOUtils.toString(new EnvironmentAwareResource(inputStream).getInputStream());

    // Assert
    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  @Test
  public void getInputStream_VariableIsReplaced_WithoutBrackets() throws IOException {
    // Arrange
    String input = "$NAME";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output = IOUtils.toString(new EnvironmentAwareResource(inputStream).getInputStream());

    // Assert
    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  @Test
  public void getInputStream_VariablAreIsReplaced_WithMultipleValues() throws IOException {
    // Arrange
    String input = "$NAME${NAME}$NAME";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output = IOUtils.toString(new EnvironmentAwareResource(inputStream).getInputStream());

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
    String output = IOUtils.toString(new EnvironmentAwareResource(inputStream).getInputStream());

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
    String output = IOUtils.toString(new EnvironmentAwareResource(inputStream).getInputStream());

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
    String output = IOUtils.toString(new EnvironmentAwareResource(inputStream).getInputStream());

    // Assert
    assertThat(output, is(input));
  }

  @Test
  public void getInputStream_VariableNotReplaced_WithFollowedByUnderscore() throws IOException {
    // Arrange
    String input = "$NAME_";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output = IOUtils.toString(new EnvironmentAwareResource(inputStream).getInputStream());

    // Assert
    assertThat(output, is(input));
  }

  @Test
  public void getInputStream_VariableIsReplaced_LongName() throws IOException {
    // Arrange
    String input = "$ENVIRONMENT_VARIABLE_IS_LONGER_THAN_VALUE";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    // Act
    String output = IOUtils.toString(new EnvironmentAwareResource(inputStream).getInputStream());

    // Assert
    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME));
  }
}
