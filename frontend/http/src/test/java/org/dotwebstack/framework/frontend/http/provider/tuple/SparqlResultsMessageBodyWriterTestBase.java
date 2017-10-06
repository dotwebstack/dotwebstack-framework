package org.dotwebstack.framework.frontend.http.provider.tuple;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.Iterator;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class SparqlResultsMessageBodyWriterTestBase {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  OutputStream outputStream;

  @Mock
  TupleQueryResult tupleQueryResult;

  @Captor
  ArgumentCaptor<byte[]> byteCaptor;

  @Test
  public void constructor_ThrowsException_ForMissingMediaType() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new AbstractSparqlResultsMessageBodyWriter(null) {

      @Override
      protected TupleQueryResultWriter createWriter(OutputStream outputStream) {
        return null;
      }
    };
  }

  void configureBindingSetWithValue(BindingSet bindingSet, String value) {
    @SuppressWarnings("unchecked")
    Iterator<Binding> iterator = mock(Iterator.class);
    when(bindingSet.iterator()).thenReturn(iterator);

    ValueFactory factory = SimpleValueFactory.getInstance();
    Binding binding = mock(Binding.class);
    when(iterator.hasNext()).thenReturn(true, false);
    when(iterator.next()).thenReturn(binding);
    when(binding.getName()).thenReturn("beer");
    when(binding.getValue()).thenReturn(factory.createLiteral(value));
  }

}
