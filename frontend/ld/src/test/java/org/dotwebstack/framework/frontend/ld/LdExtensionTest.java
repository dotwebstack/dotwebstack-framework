package org.dotwebstack.framework.frontend.ld;

import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdExtensionTest {

  @Mock
  private HttpConfiguration httpConfiguration;

  @Mock
  private RequestMapper requestMapper;

  private LdExtension ldExtension;

  @Test
  public void setUp() {
    ldExtension = new LdExtension(requestMapper);
    ldExtension.initialize(httpConfiguration);
  }
}
