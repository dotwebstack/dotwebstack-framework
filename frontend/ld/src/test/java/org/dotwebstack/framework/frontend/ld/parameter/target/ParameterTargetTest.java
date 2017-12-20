package org.dotwebstack.framework.frontend.ld.parameter.target;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParameterTargetTest {

  private ParameterTarget parameterTarget;

  @Mock
  private ParameterDefinition parameterDefinitionMock;

  @Before
  public void setUp() {
    // Arrange
    parameterTarget = new ParameterTarget(parameterDefinitionMock);
    when(parameterDefinitionMock.getName()).thenReturn(DBEERPEDIA.NAME_PARAMETER);
  }

  @Test
  public void set_AddParameterNameAndValue_WithValidValue() {
    // Act
    Map<String, String> map = parameterTarget.set(DBEERPEDIA.NAME_PARAMETER_VALUE_STRING);

    // Assert
    assertThat(map.keySet().toArray()[0], equalTo(DBEERPEDIA.NAME_PARAMETER));
    assertThat(map.values().toArray()[0], equalTo(DBEERPEDIA.NAME_PARAMETER_VALUE_STRING));
  }

}
