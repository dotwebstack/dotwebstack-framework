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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

class SparqlResultsMessageBodyWriterTestBase {

  @Mock
  OutputStream outputStream;

  @Mock
  TupleQueryResult tupleQueryResult;

  @Captor
  ArgumentCaptor<byte[]> byteCaptor;

  BindingSet configureBindingSetWithValue(BindingSet bindingSet, String value) {
    @SuppressWarnings("unchecked")
    Iterator<Binding> iterator = mock(Iterator.class);
    when(bindingSet.iterator()).thenReturn(iterator);

    ValueFactory factory = SimpleValueFactory.getInstance();
    Binding binding = mock(Binding.class);
    when(iterator.hasNext()).thenReturn(true, false);
    when(iterator.next()).thenReturn(binding);
    when(binding.getName()).thenReturn("beer");
    when(binding.getValue()).thenReturn(factory.createLiteral(value));

    return bindingSet;
  }
}
