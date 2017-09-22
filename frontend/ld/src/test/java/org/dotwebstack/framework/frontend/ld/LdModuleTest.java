package org.dotwebstack.framework.frontend.ld;

import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdModuleTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private HttpConfiguration httpConfiguration;

  @Mock
  private LdRequestMapper requestMapper;

  private LdModule ldModule;

  @Test
  public void setUp() {
    ldModule = new LdModule(requestMapper);
    ldModule.initialize(httpConfiguration);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRequestMapper() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new LdModule(null);
  }

}
