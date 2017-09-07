package org.dotwebstack.framework.frontend.ld;

import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdExtensionTest {

  @Mock
  private HttpConfiguration httpConfiguration;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  private LdExtension ldExtension;

  @Test
  public void setUp() {
    ldExtension = new LdExtension(representationResourceProvider);
    ldExtension.initialize(httpConfiguration);
  }
}
