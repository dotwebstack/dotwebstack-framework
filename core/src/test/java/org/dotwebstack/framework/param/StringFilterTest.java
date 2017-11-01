package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.dotwebstack.framework.param.template.TemplateProcessor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
 
@RunWith(MockitoJUnitRunner.class)
public class StringFilterTest {

  private static final IRI ID = SimpleValueFactory.getInstance().createIRI("http://foo#", "bar");

  @Mock
  private TemplateProcessor templateProcessorMock;

  private StringFilter filter;

  @Before
  public void setUp() {
    filter = new StringFilter(ID, "variable", templateProcessorMock);
  }

  @Test
  public void handle_ModifiesQuery_WithSingleVariable() {
    // Arrange
    String query = "SELECT ${variable}";
    String modifiedQuery = "SELECT value";

    when(templateProcessorMock.processString(query,
        ImmutableMap.of("variable", "value"))).thenReturn(modifiedQuery);

    // Act
    String result = filter.handle("value", query);

    // Assert
    assertThat(result, is(modifiedQuery));
  }

  @Test
  public void handle_ModifiesQuery_WithDotsInVariable() {
    // Arrange
    String query = "SELECT ${vari.able}";
    String modifiedQuery = "SELECT value";

    when(templateProcessorMock.processString(query,
        ImmutableMap.of("vari.able", "value"))).thenReturn(modifiedQuery);

    // Act
    StringFilter filter = new StringFilter(ID, "vari.able", templateProcessorMock);
    String result = filter.handle("value", query);

    // Assert
    assertThat(result, is(modifiedQuery));
  }

  @Test
  public void handle_ModifiesQuery_WithMultipleVariables() {
    // Arrange
    String query = "SELECT ${variable} WHERE ${variable}";
    String modifiedQuery = "SELECT value WHERE value";

    when(templateProcessorMock.processString(query,
        ImmutableMap.of("variable", "value"))).thenReturn(modifiedQuery);

    // Act
    String result = filter.handle("value", query);

    // Assert
    assertThat(result, is(modifiedQuery));
  }

  @Test
  public void handle_ModifiesQuery_WithNullVariable() {
    // Arrange
    String query = "SELECT ${variable}";
    String modifiedQuery = "SELECT null";

    when(templateProcessorMock.processString(query,
        Collections.singletonMap("variable", null))).thenReturn(modifiedQuery);

    // Act
    String result = filter.handle(null, query);

    // Assert
    assertThat(result, is(modifiedQuery));
  }

  @Test
  public void handle_DoesNotModifyQuery_WithoutVariable() {
    // Arrange
    String query = "SELECT ?noVariable";
    String unmodifiedQuery = "SELECT ?noVariable";

    when(templateProcessorMock.processString(query,
        Collections.singletonMap("variable", "value"))).thenReturn(unmodifiedQuery);

    // Act
    String result = filter.handle("value", query);

    // Assert
    assertThat(result, is(unmodifiedQuery));
  }

  @Test
  public void handle_DoesNotModifyQuery_WithoutMatchingVariable() {
    // Arrange
    String query = "SELECT ${noMatchingVariable}";
    String unmodifiedQuery = "SELECT ${noMatchingVariable}";

    when(templateProcessorMock.processString(query,
        Collections.singletonMap("variable", "value"))).thenReturn(unmodifiedQuery);

    // Act
    String result = filter.handle("value", query);

    // Assert
    assertThat(result, is(unmodifiedQuery));
  }

}
