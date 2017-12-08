package org.dotwebstack.framework.frontend.ld.parameter.source;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.dotwebstack.framework.vocabulary.HTTP;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParameterSourceFactoryTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private ParameterSourceFactory parameterSourceFactory;

  @Before
  public void setUp() {
    // Arrange
    parameterSourceFactory = new ParameterSourceFactory();
  }

  @Test
  public void newParameterSource_ThrowsException_WithUnknownIRI() {
    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    parameterSourceFactory.getParameterSource(ELMO.SITE);
  }

  @Test
  public void newParameterSource_GetParameterSourceInstance_WithValidIRI() {
    // Act
    ParameterSource parameterSource = parameterSourceFactory.getParameterSource(HTTP.REQUEST_URI);

    // Assert
    assertThat(parameterSource, instanceOf(RequestUriParameterSource.class));
  }

}
