package org.dotwebstack.framework.frontend.http.provider.tuple;

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
import org.dotwebstack.framework.frontend.http.provider.MediaTypes;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlResultsXmlMessageBodyWriterTest extends SparqlResultsMessageBodyWriterTestBase {

  @Test
  public void isWritableForSparqlResultsXmlMediaType() {
    // Arrange
    SparqlResultsXmlMessageBodyWriter provider = new SparqlResultsXmlMessageBodyWriter();

    // Act
    boolean result = provider.isWriteable(TupleQueryResult.class, null, null,
        MediaTypes.SPARQL_RESULTS_XML_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isNotWritableForStringClass() {
    // Arrange
    SparqlResultsXmlMessageBodyWriter provider = new SparqlResultsXmlMessageBodyWriter();

    // Act
    boolean result =
        provider.isWriteable(String.class, null, null, MediaTypes.SPARQL_RESULTS_XML_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isNotWritableForXmlMediaType() {
    // Arrange
    SparqlResultsXmlMessageBodyWriter provider = new SparqlResultsXmlMessageBodyWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleQueryResult.class, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void writesSparqlResultXmlFormat() throws IOException {
    // Arrange
    SparqlResultsXmlMessageBodyWriter provider = new SparqlResultsXmlMessageBodyWriter();
    when(tupleQueryResult.getBindingNames()).thenReturn(Collections.singletonList("beer"));
    when(tupleQueryResult.hasNext()).thenReturn(true, true, false);
    BindingSet bindingSetHeineken = mock(BindingSet.class);
    BindingSet bindingSetAmstel = mock(BindingSet.class);
    when(tupleQueryResult.next()).thenReturn(bindingSetHeineken, bindingSetAmstel);

    configureBindingSetWithValue(bindingSetHeineken, "Heineken");
    configureBindingSetWithValue(bindingSetAmstel, "Amstel");

    // Act
    provider.writeTo(tupleQueryResult, null, null, null, null, null, outputStream);

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
