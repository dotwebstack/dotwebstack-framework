package org.dotwebstack.framework.frontend.ld.writer.graph;

import freemarker.template.Template;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.HtmlGraphEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.result.HtmlGraphResult;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.io.OutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlGraphEntityWriterTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private OutputStream outputStream;

  @Mock
  private HtmlGraphResult htmlGraphResult;

  @Mock
  private HtmlGraphEntity htmlGraphEntity;

  @Mock
  private Representation htmlRepresentation;

  @Mock
  private Template template;

  @Captor
  private ArgumentCaptor<byte[]> byteCaptor;

  @Test
  public void isWritable_IsTrue_ForJsonLdMediaType() {
    // Arrange
    HtmlGraphEntityWriter writer = new HtmlGraphEntityWriter();

    // Act
    boolean result = writer.isWriteable(HtmlGraphEntity.class, null, null, MediaTypes.TEXT_HTML_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsTrue_ForJsonMediaType() {
    // Arrange
    HtmlGraphEntityWriter writer = new HtmlGraphEntityWriter();

    // Act
    boolean result =
        writer.isWriteable(HtmlGraphEntity.class, null, null, MediaType.TEXT_HTML_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsFalse_ForStringClass() {
    // Arrange
    HtmlGraphEntityWriter writer = new HtmlGraphEntityWriter();

    // Act
    boolean result = writer.isWriteable(String.class, null, null, MediaTypes.TEXT_HTML_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isWritable_IsFalse_ForJsonMediaType() {
    // Arrange
    HtmlGraphEntityWriter writer = new HtmlGraphEntityWriter();

    // Act
    boolean result = writer.isWriteable(HtmlGraphEntity.class, null, null, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSize_MinusOne_Always() {
    // Arrange
    HtmlGraphEntityWriter writer = new HtmlGraphEntityWriter();

    // Act
    long result = writer.getSize(htmlGraphEntity, null, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, equalTo(-1L));
  }

  @Test
  public void writeTo_JsonLdFormat_ForQueryResult() throws Exception {
    // Arrange
    final HtmlGraphEntityWriter writer = new HtmlGraphEntityWriter();
    Model model =
        new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDF.TYPE, DBEERPEDIA.BACKEND).add(
            RDFS.LABEL, DBEERPEDIA.BREWERIES_LABEL).build();

    when(htmlGraphEntity.getQueryResult()).thenReturn(htmlGraphResult);
    when(htmlGraphResult.hasNext()).thenReturn(true, true, true, false);
    when(htmlGraphResult.next()).thenReturn(model.stream().findFirst().get(),
        model.stream().skip(1).toArray(Statement[]::new));

    // Act
    writer.writeTo(htmlGraphEntity, null, null, null,
            null, null, outputStream);

    // Assert
    verify(outputStream).write(byteCaptor.capture(), anyInt(), anyInt());
    String result = new String(byteCaptor.getValue());
    final String checkResult = "[ {\n  \"@id\" : \"http://dbeerpedia.org#Breweries\",\n "
        + " \"@type\" : [ \"http://dbeerpedia.org#Backend\" ],\n "
        + " \"http://www.w3.org/2000/01/rdf-schema#label\" : [ {\n "
        + "   \"@value\" : \"Beer breweries in The Netherlands\"\n  } ]";
    assertThat(result.replace("\r\n", "\n").replace("\r", "\n"), containsString(checkResult));
  }

}
