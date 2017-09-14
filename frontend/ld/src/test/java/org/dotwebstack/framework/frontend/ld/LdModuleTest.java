package org.dotwebstack.framework.frontend.ld;

import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdModuleTest {

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
}
