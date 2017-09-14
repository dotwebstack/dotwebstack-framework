package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Arrays;
import java.util.Collections;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.graph.TurtleGraphMessageBodyWriter;
import org.dotwebstack.framework.frontend.http.provider.tuple.SparqlResultsJsonMessageBodyWriter;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SupportedMediaTypesScannerTest {

  @Mock
  private MessageBodyWriter<GraphQueryResult> unsupportedGraphWriter;

  @Mock
  private MessageBodyWriter<TupleQueryResult> unsupportedTupleWriter;

  @Test
  public void findsSupportedGraphProviders() {
    // Arrange & Act
    SupportedMediaTypesScanner scanner = new SupportedMediaTypesScanner(
        Collections.singletonList(new TurtleGraphMessageBodyWriter()), Collections.emptyList());

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(1));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.GRAPH)),
        hasItems(MediaType.valueOf("text/turtle")));
    assertThat(scanner.getSparqlProviders().size(), equalTo(1));
  }

  @Test
  public void findsSupportedTupleProviders() {
    // Arrange & Act
    SupportedMediaTypesScanner scanner = new SupportedMediaTypesScanner(Collections.emptyList(),
        Collections.singletonList(new SparqlResultsJsonMessageBodyWriter()));

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.TUPLE).length, equalTo(1));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.TUPLE)),
        hasItems(MediaType.valueOf("application/sparql-results+json")));
    assertThat(scanner.getSparqlProviders().size(), equalTo(1));
  }

  @Test
  public void ignoresForUnsupportedResultType() {
    // Arrange & Act
    SupportedMediaTypesScanner scanner =
        new SupportedMediaTypesScanner(Collections.singletonList(unsupportedGraphWriter),
            Collections.singletonList(unsupportedTupleWriter));

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.TUPLE).length, equalTo(0));
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(0));
    assertThat(scanner.getSparqlProviders().size(), equalTo(0));
  }

}
