package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TermParameterTest {


  @Test
  public void handle_ReturnsValueForRequiredFilter() {
    // Arrange
    TermParameter parameter = TermParameter.requiredTermParameter(DBEERPEDIA.PARAMETER_NAME,
        DBEERPEDIA.PARAMETER_NAME_VALUE.stringValue());
    Map<String, Object> parameterValues =
        ImmutableMap.of(DBEERPEDIA.PARAMETER_NAME_VALUE.stringValue(), "value");

    // Act
    Object result = parameter.handle(parameterValues);

    // Assert
    assertThat(result, is("value"));
  }

  @Test
  public void handle_ReturnsNullForOptionalFilter() {
    // Arrange
    TermParameter parameter = TermParameter.optionalTermParameter(DBEERPEDIA.PARAMETER_PLACE,
        DBEERPEDIA.PARAMETER_PLACE_VALUE.stringValue());
    Map<String, Object> parameterValues =
        Collections.singletonMap(DBEERPEDIA.PARAMETER_PLACE_VALUE.stringValue(), null);

    // Act
    Object result = parameter.handle(parameterValues);

    // Assert
    assertThat(result, nullValue());
  }

}
