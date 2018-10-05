package org.dotwebstack.framework.frontend.ld.writer.tuple;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlResultsCsvTupleEntityWriterTest extends SparqlResultsTupleEntityWriterTestBase {

  @Test
  public void constructor_ThrowsException_WithMissingMediaType() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new AbstractTupleEntityWriter(null) {
      @Override
      protected void write(TupleQueryResult tupleQueryResult, OutputStream outputStream)
          throws IOException {}
    };
  }

  @Test
  public void isWritable_IsTrue_ForCsvMediaType() {
    // Arrange
    SparqlResultsCsvTupleEntityWriter provider = new SparqlResultsCsvTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleEntity.class, null, null, MediaTypes.CSV_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsFalse_ForStringClass() {
    // Arrange
    SparqlResultsCsvTupleEntityWriter provider = new SparqlResultsCsvTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(String.class, null, null, MediaTypes.CSV_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isWritable_IsFalse_ForTextPlainMediaType() {
    // Arrange
    SparqlResultsCsvTupleEntityWriter provider = new SparqlResultsCsvTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleEntity.class, null, null, MediaType.TEXT_PLAIN_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSize_MinusOne_Always() {
    // Arrange
    SparqlResultsCsvTupleEntityWriter writer = new SparqlResultsCsvTupleEntityWriter();

    // Act
    long result = writer.getSize(tupleEntity, null, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, equalTo(-1L));
  }

  @Test
  public void writeTo_SparqlResultCsvFormat_ForQueryResult() throws IOException {
    // Arrange
    final SparqlResultsCsvTupleEntityWriter provider = new SparqlResultsCsvTupleEntityWriter();
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
    final String checkResult = "beer\nHeineken\nAmstel\n";
    // assertThat(result, containsString(checkResult));
    assertThat(result.replace("\r\n", "\n").replace("\r", "\n"), containsString(checkResult));
  }

  @Override
  void configureBindingSetWithValue(BindingSet bindingSet, String value) {

    ValueFactory factory = SimpleValueFactory.getInstance();
    when(bindingSet.getValue("beer")).thenReturn(factory.createLiteral(value));
  }

}
