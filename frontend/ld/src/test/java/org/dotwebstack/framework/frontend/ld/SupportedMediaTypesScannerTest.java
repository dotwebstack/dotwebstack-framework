package org.dotwebstack.framework.frontend.ld;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Arrays;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.ResultType;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SupportedMediaTypesScannerTest {

  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void findsSupportedGraphProviders() {
    // Arrange
    SupportedMediaTypesScanner scanner = new SupportedMediaTypesScanner();

    // Act
    scanner.loadSupportedMediaTypes();

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.GRAPH).length, equalTo(4));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.GRAPH)),
        hasItems(MediaType.valueOf("text/turtle"), MediaType.valueOf("application/trig"),
            MediaType.valueOf("application/ld+json"), MediaType.valueOf("application/rdf+xml")));
  }

  @Test
  public void findsSupportedTupleProviders() {
    // Arrange
    SupportedMediaTypesScanner scanner = new SupportedMediaTypesScanner();

    // Act
    scanner.loadSupportedMediaTypes();

    // Assert
    assertThat(scanner.getMediaTypes(ResultType.TUPLE).length, equalTo(2));
    assertThat(Arrays.asList(scanner.getMediaTypes(ResultType.TUPLE)),
        hasItems(MediaType.valueOf("application/sparql-results+json"),
            MediaType.valueOf("application/sparql-results+xml")));
  }

  @Test
  public void throwsForUnsupportedResultType() {
    // Arrange
    SupportedMediaTypesScanner scanner = new SupportedMediaTypesScanner();

    // Assert
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("bla");

    // Act
    scanner.loadSupportedMediaTypes();
  }

}
