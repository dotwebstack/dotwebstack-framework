package org.dotwebstack.framework.frontend.http.provider.tuple;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.http.provider.MediaTypes;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlResultsJsonMessageBodyWriterTest extends SparqlResultsMessageBodyWriterTestBase {

  @Test
  public void isWritable_IsTrue_ForSparqlResultsJsonMediaType() {
    // Arrange
    SparqlResultsJsonMessageBodyWriter provider = new SparqlResultsJsonMessageBodyWriter();

    // Act
    boolean result = provider.isWriteable(TupleQueryResult.class, null, null,
        MediaTypes.SPARQL_RESULTS_JSON_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsFalse_ForStringClass() {
    // Arrange
    SparqlResultsJsonMessageBodyWriter provider = new SparqlResultsJsonMessageBodyWriter();

    // Act
    boolean result =
        provider.isWriteable(String.class, null, null, MediaTypes.SPARQL_RESULTS_JSON_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isWritable_IsFalse_ForJsonMediaType() {
    // Arrange
    SparqlResultsJsonMessageBodyWriter provider = new SparqlResultsJsonMessageBodyWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleQueryResult.class, null, null, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSize_MinusOne_Always() {
    // Arrange
    SparqlResultsJsonMessageBodyWriter writer = new SparqlResultsJsonMessageBodyWriter();

    // Act
    long result =
        writer.getSize(tupleQueryResult, null, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, equalTo(-1L));
  }

  @Test
  public void writeTo_SparqlResultJsonFormat_ForQueryResult() throws IOException {
    // Arrange
    SparqlResultsJsonMessageBodyWriter provider = new SparqlResultsJsonMessageBodyWriter();
    when(tupleQueryResult.getBindingNames()).thenReturn(Arrays.asList("beer"));
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
    assertThat(result, containsString("{\"head\":{\"vars\":[\"beer\"]}"));
    assertThat(result,
        containsString(
            "{\"bindings\":" + "[{\"beer\":{\"type\":\"literal\",\"value\":\"Heineken\"}},"
                + "{\"beer\":{\"type\":\"literal\",\"value\":\"Amstel\"}}]}}"));
  }

}
