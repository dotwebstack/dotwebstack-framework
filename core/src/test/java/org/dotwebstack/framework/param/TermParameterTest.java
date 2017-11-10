package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TermParameterTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private TermParameter requiredParameter;
  private TermParameter optionalParameter;

  @Before
  public void setUp() {
    requiredParameter = TermParameter.requiredTermParameter(DBEERPEDIA.PARAMETER_NAME,
        DBEERPEDIA.PARAMETER_NAME_NAME_LITERAL_STRING);
    optionalParameter = TermParameter.optionalTermParameter(DBEERPEDIA.PARAMETER_PLACE,
        DBEERPEDIA.PARAMETER_PLACE_NAME_LITERAL_STRING);
  }

  @Test
  public void handle_ReturnsValueForRequiredFilter() {
    // Arrange
    Map<String, Object> parameterValues =
        ImmutableMap.of(DBEERPEDIA.PARAMETER_NAME_NAME_LITERAL_STRING, "value");

    // Act
    Object result = requiredParameter.handle(parameterValues);

    // Assert
    assertThat(result, is("value"));
  }

  @Test
  public void handle_ReturnsNullForOptionalFilter() {
    // Arrange
    Map<String, Object> parameterValues =
        Collections.singletonMap(DBEERPEDIA.PARAMETER_PLACE_NAME_LITERAL_STRING, null);

    // Act
    Object result = optionalParameter.handle(parameterValues);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void validate_RejectsNullValue_ForRequiredParameter() {
    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(
        String.format("No value found for required parameter '%s'. Supplied parameterValues:",
            DBEERPEDIA.PARAMETER_NAME));

    // Act
    requiredParameter.validate(ImmutableMap.of());
  }

  @Test
  public void validate_AcceptsNonNullValue_ForRequiredParameter() {
    // Act
    requiredParameter.validate(ImmutableMap.of(DBEERPEDIA.PARAMETER_NAME_NAME_LITERAL_STRING,
        DBEERPEDIA.BREWERY_DAVO_NAME));
  }

  @Test
  public void validate_AcceptsNullValue_ForOptionalParameter() {
    // Act
    optionalParameter.validate(ImmutableMap.of());
  }

  @Test
  public void validate_RejectsNonStringValue_ForTermParameter() {
    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(
        String.format("Value for parameter '%s' not a String:", DBEERPEDIA.PARAMETER_NAME));

    // Act
    requiredParameter.validate(
        ImmutableMap.of(DBEERPEDIA.PARAMETER_NAME_NAME_LITERAL_STRING, new Object()));
  }

}
