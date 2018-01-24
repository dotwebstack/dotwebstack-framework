package org.dotwebstack.framework.frontend.ld.writer.tuple;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.eclipse.rdf4j.query.BindingSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlResultsXmlTupleEntityWriterTest extends SparqlResultsTupleEntityWriterTestBase {

  @Test
  public void isWritable_IsTrue_ForSparqlResultsXmlMediaType() {
    // Arrange
    SparqlResultsXmlTupleEntityWriter provider = new SparqlResultsXmlTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleEntity.class, null, null, MediaTypes.SPARQL_RESULTS_XML_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsFalse_ForStringClass() {
    // Arrange
    SparqlResultsXmlTupleEntityWriter provider = new SparqlResultsXmlTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(String.class, null, null, MediaTypes.SPARQL_RESULTS_XML_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isWritable_IsFalse_ForXmlMediaType() {
    // Arrange
    SparqlResultsXmlTupleEntityWriter provider = new SparqlResultsXmlTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleEntity.class, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void writeTo_SparqlResultXmlFormat_ForQueryResult() throws IOException {
    // Arrange
    final SparqlResultsXmlTupleEntityWriter provider = new SparqlResultsXmlTupleEntityWriter();
    when(tupleEntity.getQueryResult()).thenReturn(tupleQueryResult);
    when(tupleQueryResult.getBindingNames()).thenReturn(Collections.singletonList("beer"));
    when(tupleQueryResult.hasNext()).thenReturn(true, true, false);
    BindingSet bindingSetHeineken = mock(BindingSet.class);
    BindingSet bindingSetAmstel = mock(BindingSet.class);
    when(tupleQueryResult.next()).thenReturn(bindingSetHeineken, bindingSetAmstel);

    configureBindingSetWithValue(bindingSetHeineken, "Heineken");
    configureBindingSetWithValue(bindingSetAmstel, "Amstel");

    // Act
    provider.writeTo(tupleEntity, null, null, null, null, null, outputStream);

    // Assert
    verify(outputStream).write(byteCaptor.capture(), anyInt(), anyInt());
    String result = new String(byteCaptor.getValue());
    assertThat(result, containsString("sparql xmlns='http://www.w3.org/2005/sparql-results#'"));
    assertThat(result, containsString("<head><variable name='beer'/></head>"));
    assertThat(result,
        containsString("<results>"
            + "<result><binding name='beer'><literal>Heineken</literal></binding></result>"
            + "<result><binding name='beer'><literal>Amstel</literal></binding></result>"
            + "</results>"));
  }

}
