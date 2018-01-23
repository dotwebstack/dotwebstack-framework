package org.dotwebstack.framework.frontend.ld.writer.tuple;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.eclipse.rdf4j.query.BindingSet;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XmlTupleEntityWriterTest extends SparqlResultsTupleEntityWriterTestBase {

  @Test
  public void isWritable_IsTrue_ForXmlMediaType() {
    // Arrange
    XmlTupleEntityWriter provider = new XmlTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleEntity.class, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsFalse_ForStringClass() {
    // Arrange
    XmlTupleEntityWriter provider = new XmlTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(String.class, null, null, MediaTypes.SPARQL_RESULTS_JSON_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isWritable_IsFalse_ForJsonMediaType() {
    // Arrange
    XmlTupleEntityWriter provider = new XmlTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleEntity.class, null, null, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSize_MinusOne_Always() {
    // Arrange
    XmlTupleEntityWriter writer = new XmlTupleEntityWriter();

    // Act
    long result = writer.getSize(tupleEntity, null, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, equalTo(-1L));
  }

  @Test
  public void writeTo_XmlFormat_ForQueryResult() throws IOException, JSONException {
    // Arrange
    final XmlTupleEntityWriter provider = new XmlTupleEntityWriter();
    when(tupleEntity.getQueryResult()).thenReturn(tupleQueryResult);
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

    assertThat(result, containsString("<results>"));
    assertThat(result, containsString("</results>"));
    assertThat(result, containsString("<result>"));
    assertThat(result, containsString("<beer>Heineken</beer>"));
    assertThat(result, containsString("<beer>Amstel</beer>"));
  }

}
