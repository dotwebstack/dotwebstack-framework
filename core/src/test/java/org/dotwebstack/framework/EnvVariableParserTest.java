package org.dotwebstack.framework;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
@PrepareForTest( {EnvVariableParser.class})
public class EnvVariableParserTest {

  EnvVariableParser envVariableParser;

  @Before
  public void setUp() throws Exception {

    Map<String, String> env = new HashMap<>();
    env.put("NAME", DBEERPEDIA.BREWERY_DAVO_NAME);

    PowerMockito.mockStatic(System.class);
    when(System.getenv()).thenReturn(env);

    envVariableParser = new EnvVariableParser();
  }

  @Test
  public void parseWithBrackets() throws Exception {
    String input = "${NAME}";

    String output = envVariableParser.parse(input);

    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  @Test
  public void parseWithoutBrackets() throws Exception {
    String input = "$NAME";

    String output = envVariableParser.parse(input);

    assertThat(output, is(DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  @Test
  public void parseWithoutBracketsAndShouldNotMatch() throws Exception {
    String input = "$NAMES";

    String output = envVariableParser.parse(input);

    assertThat(output, is(input));
  }

  @Test
  public void parseInputStream() {
    String input = "${NAME}";
    InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

    InputStream output = null;
    try {
      output = envVariableParser.parse(inputStream);

      InputStream expectedOutputStream =
          new ByteArrayInputStream(DBEERPEDIA.BREWERY_DAVO_NAME.getBytes(StandardCharsets.UTF_8));
      assertTrue(IOUtils.contentEquals(output, expectedOutputStream));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
