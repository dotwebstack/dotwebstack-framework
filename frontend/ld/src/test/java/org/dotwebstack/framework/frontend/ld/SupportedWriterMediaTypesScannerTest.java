package org.dotwebstack.framework.frontend.ld;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.MessageBodyWriter;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.dotwebstack.framework.frontend.ld.writer.graph.GraphEntityWriter;
import org.dotwebstack.framework.frontend.ld.writer.tuple.AbstractTupleEntityWriter;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SupportedWriterMediaTypesScannerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private MessageBodyWriter<GraphEntity> unsupportedGraphWriter;

  @Mock
  private MessageBodyWriter<TupleEntity> unsupportedTupleWriter;

  @Test
  public void constructor_ThrowsException_WithMissingGraphWriters() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SupportedWriterMediaTypesScanner(null, Collections.emptyList());
  }

  @Test
  public void constructor_ThrowsException_WithMissingTupleWriters() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SupportedWriterMediaTypesScanner(Collections.emptyList(), null);
  }

  @Test
  public void constructor_FindsSupportedGraphProviders_WhenProvided() {
    // Arrange & Act
    SupportedWriterMediaTypesScanner scanner = new SupportedWriterMediaTypesScanner(
        Collections.singletonList(new StubGraphEntityWriter()), Collections.emptyList());

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(1));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.GRAPH)),
        IsCollectionContaining.hasItems(MediaTypes.LDJSON_TYPE));
    assertThat(scanner.getGraphEntityWriters().size(), equalTo(1));
  }

  @Test
  public void constructor_FindsSupportedTupleProviders_WhenProvided() {
    // Arrange & Act
    SupportedWriterMediaTypesScanner scanner = new SupportedWriterMediaTypesScanner(
        Collections.emptyList(), Collections.singletonList(new StubTupleMessageBodyWriter()));

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.TUPLE).length, equalTo(1));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.TUPLE)),
        IsCollectionContaining.hasItems(MediaTypes.SPARQL_RESULTS_JSON_TYPE));
    assertThat(scanner.getTupleEntityWriters().size(), equalTo(1));
  }

  @Test
  public void constructor_IgnoresUnsupportedProviders_WhenProvided() {
    // Arrange & Act
    SupportedWriterMediaTypesScanner scanner =
        new SupportedWriterMediaTypesScanner(Collections.singletonList(unsupportedGraphWriter),
            Collections.singletonList(unsupportedTupleWriter));

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.TUPLE).length, equalTo(0));
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(0));
    assertThat(scanner.getGraphEntityWriters().size(), equalTo(0));
    assertThat(scanner.getTupleEntityWriters().size(), equalTo(0));
  }

  @Test
  public void constructor_IgnoresProviderWithoutProduce_WhenProvided() {
    // Arrange & Act
    SupportedWriterMediaTypesScanner scanner = new SupportedWriterMediaTypesScanner(
        Collections.singletonList(new InvalidGraphEntityWriter()), Collections.emptyList());

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(0));
    assertThat(scanner.getGraphEntityWriters().size(), equalTo(0));
  }

  @EntityWriter(resultType = ResultType.GRAPH)
  static class InvalidGraphEntityWriter extends GraphEntityWriter {

    InvalidGraphEntityWriter() {
      super(RDFFormat.JSONLD);
    }
  }

  @EntityWriter(resultType = ResultType.GRAPH)
  @Produces(MediaTypes.LDJSON)
  static class StubGraphEntityWriter extends GraphEntityWriter {

    StubGraphEntityWriter() {
      super(RDFFormat.JSONLD);
    }
  }

  @EntityWriter(resultType = ResultType.TUPLE)
  @Produces(MediaTypes.SPARQL_RESULTS_JSON)
  static class StubTupleMessageBodyWriter extends AbstractTupleEntityWriter {

    StubTupleMessageBodyWriter() {
      super(MediaTypes.SPARQL_RESULTS_JSON_TYPE);
    }

    @Override
    protected void write(TupleQueryResult tupleQueryResult, OutputStream outputStream)
        throws IOException {

    }
  }
}
