package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
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
    requiredParameter = TermParameter.requiredTermParameter(DBEERPEDIA.NAME_PARAMETER_ID,
        DBEERPEDIA.NAME_PARAMETER_VALUE_STRING);
    optionalParameter = TermParameter.optionalTermParameter(DBEERPEDIA.PLACE_PARAMETER_ID,
        DBEERPEDIA.PLACE_PARAMETER_VALUE_STRING);
  }

  @Test
  public void handle_ReturnsValueForRequiredFilter() {
    // Arrange
    Map<String, Object> parameterValues =
        ImmutableMap.of(DBEERPEDIA.NAME_PARAMETER_VALUE_STRING, "value");

    // Act
    String result = requiredParameter.handle(parameterValues);

    // Assert
    assertThat(result, is("value"));
  }

  @Test
  public void handle_ReturnsNullForOptionalFilter() {
    // Arrange
    Map<String, Object> parameterValues =
        Collections.singletonMap(DBEERPEDIA.PLACE_PARAMETER_VALUE_STRING, null);

    // Act
    String result = optionalParameter.handle(parameterValues);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void validate_RejectsNullValue_ForRequiredParameter() {
    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(
        String.format("No value found for required parameter '%s'. Supplied parameterValues:",
            DBEERPEDIA.NAME_PARAMETER_ID));

    // Act
    requiredParameter.validate(ImmutableMap.of());
  }

  @Test
  public void validate_AcceptsNonNullValue_ForRequiredParameter() {
    // Act
    requiredParameter.validate(
        ImmutableMap.of(DBEERPEDIA.NAME_PARAMETER_VALUE_STRING, DBEERPEDIA.BREWERY_DAVO_NAME));
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
        String.format("Value for parameter '%s' not a String:", DBEERPEDIA.NAME_PARAMETER_ID));

    // Act
    requiredParameter.validate(
        ImmutableMap.of(DBEERPEDIA.NAME_PARAMETER_VALUE_STRING, new Object()));
  }

  @Test
  public void getLiteral_ReturnsLiteral_ForValue() {
    // Arrange
    Map<String, Object> parameterValues =
        ImmutableMap.of(DBEERPEDIA.NAME_PARAMETER_VALUE_STRING, "value");

    // Act
    String value = requiredParameter.handle(parameterValues);
    Literal result = requiredParameter.getLiteral(value);

    // Assert
    assertThat(result, is(SimpleValueFactory.getInstance().createLiteral("value")));
  }

}
