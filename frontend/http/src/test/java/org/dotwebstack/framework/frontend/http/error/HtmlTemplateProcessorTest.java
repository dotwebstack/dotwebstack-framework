package org.dotwebstack.framework.frontend.http.error;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.io.OutputStream;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.frontend.http.error.ProblemDetails.Builder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

@RunWith(MockitoJUnitRunner.class)
public class HtmlTemplateProcessorTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private OutputStream outputStream;

  @Captor
  private ArgumentCaptor<byte[]> byteCaptor;

  @Mock
  private ApplicationProperties applicationProperties;

  private ResourceLoader resourceLoader;

  private ProblemDetails problemDetails;

  private HtmlTemplateProcessor processor;

  @Before
  public void setUp() throws Exception {
    resourceLoader =
        mock(ResourceLoader.class, withSettings().extraInterfaces(ResourcePatternResolver.class));
    when(applicationProperties.getResourcePath()).thenReturn("file:config");

    processor = new HtmlTemplateProcessor(applicationProperties);
    processor.setResourceLoader(resourceLoader);
    processor.afterPropertiesSet();

    Builder builder = ProblemDetails.builder()//
        .withStatus(500)//
        .withTitle("Internal error")//
        .withDetail("Something went wrong");
    problemDetails = builder.build();
  }

  @Test
  public void process_fallback() throws IOException {
    // Arrange

    // Act
    processor.process(problemDetails, outputStream);

    // Assert
    verify(outputStream).write(byteCaptor.capture(), anyInt(), anyInt());
    String result = new String(byteCaptor.getValue());
    assertThat(result, containsString("<HTML><TITLE>Internal error</TITLE><BODY>"
        + "<H1>500 Internal error</H1><P>Something went wrong</P></BODY></HTML>"));

  }

}
