package org.dotwebstack.framework.frontend.http.provider.tuple;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonFactory;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.http.jackson.AbstractTupleQueryResultSerializer;
import org.dotwebstack.framework.frontend.http.provider.MediaTypes;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(MockitoJUnitRunner.class)
public class JsonMessageBodyWriterTest extends SparqlResultsMessageBodyWriterTestBase {

  @Test
  public void constructor_ThrowsException_ForMissingMediaType() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new AbstractJsonGeneratorMessageBodyWriter(null) {

      @Override
      protected JsonFactory createFactory() {
        return null;
      }

      @Override
      protected AbstractTupleQueryResultSerializer createSerializer() {
        return null;
      }
    };
  }

  @Test
  public void isWritable_IsTrue_ForJsonMediaType() {
    // Arrange
    JsonMessageBodyWriter provider = new JsonMessageBodyWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleQueryResult.class, null, null, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsFalse_ForStringClass() {
    // Arrange
    JsonMessageBodyWriter provider = new JsonMessageBodyWriter();

    // Act
    boolean result =
        provider.isWriteable(String.class, null, null, MediaTypes.SPARQL_RESULTS_JSON_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isWritable_IsFalse_ForXmlMediaType() {
    // Arrange
    JsonMessageBodyWriter provider = new JsonMessageBodyWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleQueryResult.class, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSize_MinusOne_Always() {
    // Arrange
    JsonMessageBodyWriter writer = new JsonMessageBodyWriter();

    // Act
    long result =
        writer.getSize(tupleQueryResult, null, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, equalTo(-1L));
  }

  @Test
  public void writeTo_SparqlResultJsonFormat_ForQueryResult() throws IOException, JSONException {
    // Arrange
    JsonMessageBodyWriter provider = new JsonMessageBodyWriter();
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

    JSONArray expected = new JSONArray().put(new JSONObject().put("beer", "Heineken")).put(
        new JSONObject().put("beer", "Amstel"));
    JSONAssert.assertEquals(expected.toString(), result, true);
  }

}
