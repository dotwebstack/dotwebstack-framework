package org.dotwebstack.framework.frontend.ld.writer.tuple;

import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.HtmlTupleEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.result.HtmlTupleResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class HtmlResultsJsonTupleEntityWriterTest extends SparqlResultsTupleEntityWriterTestBase {

  @Mock
  HtmlTupleEntity htmlTupleEntity;

  @Mock
  HtmlTupleResult htmlTupleResult;

  @Test
  public void isWritable_IsTrue_ForHtmlMediaType() {
    // Arrange
    HtmlTupleEntityWriter provider = new HtmlTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(HtmlTupleEntity.class, null, null, MediaTypes.TEXT_HTML_TYPE);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void isWritable_IsFalse_ForStringClass() {
    // Arrange
    HtmlTupleEntityWriter provider = new HtmlTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(String.class, null, null, MediaTypes.SPARQL_RESULTS_JSON_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void isWritable_IsFalse_ForJsonMediaType() {
    // Arrange
    HtmlTupleEntityWriter provider = new HtmlTupleEntityWriter();

    // Act
    boolean result =
        provider.isWriteable(TupleEntity.class, null, null, MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSize_MinusOne_Always() {
    // Arrange
    HtmlTupleEntityWriter writer = new HtmlTupleEntityWriter();

    // Act
    long result = writer.getSize(htmlTupleEntity, null, null, null, MediaType.APPLICATION_XML_TYPE);

    // Assert
    assertThat(result, equalTo(-1L));
  }

}
