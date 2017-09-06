package org.dotwebstack.framework.frontend.ld;

import static org.mockito.Mockito.verifyZeroInteractions;

import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.junit.Before;
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

  @Before
  public void setUp() {
    ldExtension = new LdExtension(representationResourceProvider);
    ldExtension.initialize(httpConfiguration);
  }

  @Test
  public void postLoadDoesNothing() {
    // Act
    ldExtension.postLoad();

    // Assert
    verifyZeroInteractions(httpConfiguration);
  }

}
