package org.dotwebstack.framework.frontend.ld.parameter.target;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.dotwebstack.framework.param.ParameterDefinitionResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TargetFactoryTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private TargetFactory targetFactory;

  @Mock
  private ParameterDefinitionResourceProvider parameterDefinitionResourceProvider;

  @Mock
  private ParameterDefinition parameterDefinition;

  @Before
  public void setUp() {
    // Arrange
    targetFactory = new TargetFactory(parameterDefinitionResourceProvider);

    when(parameterDefinitionResourceProvider.get(DBEERPEDIA.PLACE_PARAMETER_ID)).thenReturn(
        parameterDefinition);
  }

  @Test
  public void newParameterSource_ThrowsException_WithUnknownIRI() {
    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    targetFactory.getTarget(DBEERPEDIA.NAME_PARAMETER_ID);
  }

  @Test
  public void newParameterSource_GetParameterSourceInstance_WithValidIRI() {
    // Act/Assert
    assertThat(targetFactory.getTarget(DBEERPEDIA.PLACE_PARAMETER_ID), instanceOf(Target.class));
  }

}
