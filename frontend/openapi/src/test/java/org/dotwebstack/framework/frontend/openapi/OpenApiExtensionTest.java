package org.dotwebstack.framework.frontend.openapi;

import static org.mockito.Mockito.verifyZeroInteractions;

import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenApiExtensionTest {

  @Mock
  private HttpConfiguration httpConfiguration;

  private OpenApiExtension openApiExtension;

  @Before
  public void setUp() {
    openApiExtension = new OpenApiExtension(httpConfiguration);
  }

  @Test
  public void postLoadDoesNothing() {
    // Act
    openApiExtension.postLoad();

    // Assert
    verifyZeroInteractions(httpConfiguration);
  }

}
