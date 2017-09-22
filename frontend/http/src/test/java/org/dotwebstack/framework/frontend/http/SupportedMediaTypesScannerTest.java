package org.dotwebstack.framework.frontend.http;

import static junit.framework.TestCase.fail;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SupportedMediaTypesScannerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private MessageBodyWriter<GraphQueryResult> unsupportedGraphWriter;

  @Mock
  private MessageBodyWriter<TupleQueryResult> unsupportedTupleWriter;

  @Test
  public void constructor_ThrowsException_WithMissingGraphWriters() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SupportedMediaTypesScanner(null, Collections.emptyList());
  }

  @Test
  public void constructor_ThrowsException_WithMissingTupleWriters() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SupportedMediaTypesScanner(Collections.emptyList(), null);
  }

  @Test
  public void constructor_FindsSupportedGraphProviders_WhenProvided() {
    // Arrange & Act
    SupportedMediaTypesScanner scanner = new SupportedMediaTypesScanner(
        Collections.singletonList(new StubGraphMessageBodyWriter()), Collections.emptyList());

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(1));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.GRAPH)),
        hasItems(MediaTypes.LDJSON_TYPE));
    assertThat(scanner.getGraphQueryWriters().size(), equalTo(1));
  }

  @Test
  public void constructor_FindsSupportedTupleProviders_WhenProvided() {
    // Arrange & Act
    SupportedMediaTypesScanner scanner = new SupportedMediaTypesScanner(Collections.emptyList(),
        Collections.singletonList(new StubTupleMessageBodyWriter()));

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.TUPLE).length, equalTo(1));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.TUPLE)),
        hasItems(MediaTypes.SPARQL_RESULTS_JSON_TYPE));
    assertThat(scanner.getTupleQueryWriters().size(), equalTo(1));
  }

  @Test
  public void constructor_IgnoresUnsupportedProviders_WhenProvided() {
    // Arrange & Act
    SupportedMediaTypesScanner scanner =
        new SupportedMediaTypesScanner(Collections.singletonList(unsupportedGraphWriter),
            Collections.singletonList(unsupportedTupleWriter));

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.TUPLE).length, equalTo(0));
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(0));
    assertThat(scanner.getGraphQueryWriters().size(), equalTo(0));
    assertThat(scanner.getTupleQueryWriters().size(), equalTo(0));
  }

  @Test
  public void constructor_IgnoresProviderWithoutProduce_WhenProvided() {
    // Arrange & Act
    SupportedMediaTypesScanner scanner = new SupportedMediaTypesScanner(
        Collections.singletonList(new InvalidGraphMessageBodyWriter()), Collections.emptyList());

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(0));
    assertThat(scanner.getGraphQueryWriters().size(), equalTo(0));
  }

  @SparqlProvider(resultType = ResultType.GRAPH)
  static class InvalidGraphMessageBodyWriter extends GraphMessageBodyWriter {
    InvalidGraphMessageBodyWriter() {
      super(RDFFormat.JSONLD);
    }
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
      fail("Stub class should not be used");
      return null;
    }
  }
}
