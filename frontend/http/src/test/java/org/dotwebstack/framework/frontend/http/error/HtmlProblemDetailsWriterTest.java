package org.dotwebstack.framework.frontend.http.error;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.ApplicationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HtmlProblemDetailsWriterTest {

  @Mock
  private HtmlTemplateProcessor htmlTemplateProcessor;

  @Mock
  private ProblemDetails problemDetails;

  @Mock
  private OutputStream outputStream;

  @Captor
  private ArgumentCaptor<byte[]> byteCaptor;

  @Mock
  private ApplicationProperties applicationProperties;

  @Test
  public void isWritable_IsTrue_ForHtmlMediaType() {
    // Arrange
    HtmlProblemDetailsWriter writer = new HtmlProblemDetailsWriter(htmlTemplateProcessor);

    // Act
    boolean result = writer.isWriteable(ProblemDetails.class, null, null, MediaType.TEXT_HTML_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsFalse_ForStringClass() {
    // Arrange
    HtmlProblemDetailsWriter writer = new HtmlProblemDetailsWriter(htmlTemplateProcessor);

    // Act
    boolean result = writer.isWriteable(String.class, null, null, MediaType.TEXT_HTML_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isWritable_IsFalse_ForTxtMediaType() {
    // Arrange
    HtmlProblemDetailsWriter writer = new HtmlProblemDetailsWriter(htmlTemplateProcessor);

    // Act
    boolean result = writer.isWriteable(ProblemDetails.class, null, null,
        MediaType.TEXT_PLAIN_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void writeTo_GenericError() throws IOException {
    // Arrange
    HtmlTemplateProcessor realHtmlTemplateProcessor = new HtmlTemplateProcessor(
        applicationProperties);
    final HtmlProblemDetailsWriter writer = new HtmlProblemDetailsWriter(realHtmlTemplateProcessor);

    when(problemDetails.getTitle()).thenReturn("Internal error");
    when(problemDetails.getStatus()).thenReturn(500);
    when(problemDetails.getDetail()).thenReturn("Something went wrong");

    // Act
    writer.writeTo(problemDetails, null, null, null, null, null, outputStream);

    // Assert
    verify(outputStream).write(byteCaptor.capture(), anyInt(), anyInt());
    String result = new String(byteCaptor.getValue());
    assertThat(result, containsString("<HTML><TITLE>Internal error</TITLE><BODY>"
        + "<H1>500 Internal error</H1><P>Something went wrong</P></BODY></HTML>"));
  }

}
