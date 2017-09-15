package org.dotwebstack.framework.frontend.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.MessageBodyWriter;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.MediaTypes;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.dotwebstack.framework.frontend.http.provider.graph.GraphMessageBodyWriter;
import org.dotwebstack.framework.frontend.http.provider.tuple.TupleMessageBodyWriter;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.rio.RDFFormat;
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
        Collections.singletonList(new StubGraphMessageBodyWriter()), Collections.emptyList());

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(1));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.GRAPH)),
        hasItems(MediaTypes.LDJSON_TYPE));
    assertThat(scanner.getSparqlProviders().size(), equalTo(1));
  }

  @Test
  public void findsSupportedTupleProviders() {
    // Arrange & Act
    SupportedMediaTypesScanner scanner = new SupportedMediaTypesScanner(Collections.emptyList(),
        Collections.singletonList(new StubTupleMessageBodyWriter()));

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.TUPLE).length, equalTo(1));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.TUPLE)),
        hasItems(MediaTypes.SPARQL_RESULTS_JSON_TYPE));
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



  @SparqlProvider(resultType = ResultType.GRAPH)
  @Produces(MediaTypes.LDJSON)
  static class StubGraphMessageBodyWriter extends GraphMessageBodyWriter {

    StubGraphMessageBodyWriter() {
      super(RDFFormat.JSONLD);
    }
  }

  @SparqlProvider(resultType = ResultType.TUPLE)
  @Produces(MediaTypes.SPARQL_RESULTS_JSON)
  static class StubTupleMessageBodyWriter extends TupleMessageBodyWriter {

    StubTupleMessageBodyWriter() {
      super(MediaTypes.SPARQL_RESULTS_JSON_TYPE);
    }

    @Override
    protected TupleQueryResultWriter createWriter(OutputStream outputStream) {
      throw new IllegalStateException(
          String.format("%s is not meant to be instantiated.", StubTupleMessageBodyWriter.class));
    }
  }

}
