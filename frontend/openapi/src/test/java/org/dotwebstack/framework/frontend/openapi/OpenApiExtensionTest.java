package org.dotwebstack.framework.frontend.openapi;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenApiExtensionTest {

  @Mock
  private SwaggerImporter swaggerImporter;

  private OpenApiExtension openApiExtension;

  @Before
  public void setUp() {
    openApiExtension = new OpenApiExtension(swaggerImporter);
  }

  @Test
  public void postLoadDoesNothing() {
    // Act
    openApiExtension.postLoad();

    // Assert
    verify(swaggerImporter).importDefinitions();
  }

}
