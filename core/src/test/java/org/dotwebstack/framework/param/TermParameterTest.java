package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TermParameterTest {

  private static final String NAME = DBEERPEDIA.PARAMETER_NAME_VALUE.stringValue();

  private TermParameter parameter;

  @Before
  public void setUp() {
    parameter = new TermParameter(DBEERPEDIA.PARAMETER, NAME);
  }

  @Test
  public void handle_ReturnsValueForRequiredFilter() {
    // Arrange
    Map<String, Object> parameterValues = ImmutableMap.of(NAME, "value");

    // Act
    Object result = parameter.handle(parameterValues);

    // Assert
    assertThat(result, is("value"));
  }

  @Test
  public void handle_ReturnsNullForOptionalFilter() {
    // Arrange
    Map<String, Object> parameterValues = Collections.singletonMap(NAME, null);

    // Act
    Object result = parameter.handle(parameterValues);

    // Assert
    assertThat(result, nullValue());
  }

}
