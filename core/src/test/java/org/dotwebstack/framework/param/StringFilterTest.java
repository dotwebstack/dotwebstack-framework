package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringFilterTest {

  private static final IRI ID = SimpleValueFactory.getInstance().createIRI("http://foo#", "bar");

  private static final String NAME = "name";

  private StringFilter filter;

  @Before
  public void setUp() {
    filter = new StringFilter(ID, NAME);
  }

  @Test
  public void handle_ReturnsValueForRequiredFilter() {
    // Arrange
    Map<String, Object> parameterValues = ImmutableMap.of(NAME, "value");

    // Act
    Object result = filter.handle(parameterValues);

    // Assert
    assertThat(result, is("value"));
  }

  @Test
  public void handle_ReturnsNullForOptionalFilter() {
    // Arrange
    Map<String, Object> parameterValues = Collections.singletonMap(NAME, null);

    // Act
    Object result = filter.handle(parameterValues);

    // Assert
    assertThat(result, nullValue());
  }

}
