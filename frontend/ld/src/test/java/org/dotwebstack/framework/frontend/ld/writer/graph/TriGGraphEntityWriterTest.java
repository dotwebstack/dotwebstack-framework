package org.dotwebstack.framework.frontend.ld.writer.graph;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TriGGraphEntityWriterTest {

  @Mock
  private OutputStream outputStream;

  @Mock
  private GraphEntity graphEntity;

  @Mock
  private GraphQueryResult graphQueryResult;

  @Captor
  private ArgumentCaptor<byte[]> byteCaptor;

  @Test
  public void isWritable_IsTrue_ForTriGMediaType() {
    // Arrange
    TriGGraphEntityWriter writer = new TriGGraphEntityWriter();

    // Act
    boolean result = writer.isWriteable(GraphEntity.class, null, null, MediaTypes.TRIG_TYPE);



    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsFalse_ForStringClass() {
    // Arrange
    TriGGraphEntityWriter writer = new TriGGraphEntityWriter();

    // Act
    boolean result = writer.isWriteable(String.class, null, null, MediaTypes.TRIG_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isWritable_IsFalse_ForTxtMediaType() {
    // Arrange
    TriGGraphEntityWriter writer = new TriGGraphEntityWriter();

    // Act
    boolean result = writer.isWriteable(GraphEntity.class, null, null, MediaType.TEXT_PLAIN_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void writeTo_TriGFormat_ForQueryResult() throws IOException {
    // Arrange
    final TriGGraphEntityWriter writer = new TriGGraphEntityWriter();
    Model model =
        new ModelBuilder().subject(DBEERPEDIA.BREWERIES).add(RDF.TYPE, DBEERPEDIA.BACKEND).add(
            RDFS.LABEL, DBEERPEDIA.BREWERIES_LABEL).build();

    when(graphEntity.getQueryResult()).thenReturn(graphQueryResult);
    when(graphQueryResult.hasNext()).thenReturn(true, true, true, false);
    when(graphQueryResult.next()).thenReturn(model.stream().findFirst().get(),
        model.stream().skip(1).toArray(Statement[]::new));

    // Act
    writer.writeTo(graphEntity, null, null, null, null, null, outputStream);

    // Assert
    // 2 times? feels like weird behaviour of the TriG parser
    verify(outputStream, times(2)).write(byteCaptor.capture(), anyInt(), anyInt());
    List<byte[]> values = byteCaptor.getAllValues();
    String result1 = new String(values.get(0));
    String result2 = new String(values.get(1));

    assertThat(result1,
        containsString("<http://dbeerpedia.org#Breweries> a <http://dbeerpedia.org#Backend> ;"));
    assertThat(result1, containsString(
        "<http://www.w3.org/2000/01/rdf-schema#label> \"Beer breweries in The Netherlands\""));

    assertThat(result2,
        containsString("<http://dbeerpedia.org#Breweries> a <http://dbeerpedia.org#Backend> ;"));
    assertThat(result2, containsString(
        "<http://www.w3.org/2000/01/rdf-schema#label> \"Beer breweries in The Netherlands\""));
  }

}
