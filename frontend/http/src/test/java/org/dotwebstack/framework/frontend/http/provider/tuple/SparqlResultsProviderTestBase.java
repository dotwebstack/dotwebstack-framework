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

/**
 * Created by Rick Fleuren on 9/5/2017.
 */
public class SparqlResultsProviderTestBase {

  @Mock
  protected OutputStream outputStream;
  @Mock
  protected TupleQueryResult tupleQueryResult;
  @Captor
  protected ArgumentCaptor<byte[]> byteCaptor;

  protected BindingSet configureBindingSetWithValue(BindingSet bindingSet, String value) {

    Iterator iterator = mock(Iterator.class);
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
