package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import io.swagger.parser.SwaggerParser;
import org.junit.Before;
import org.junit.Test;

public class OpenApiConfigurationTest {

  private OpenApiConfiguration openApiConfiguration;

  @Before
  public void setUp() {
    openApiConfiguration = new OpenApiConfiguration();
  }

  @Test
  public void openApiParser_ThrowsNoExceptions_WhenParsed() {
    // Act
    SwaggerParser swaggerParser = openApiConfiguration.openApiParser();

    // Assert
    assertThat(swaggerParser, notNullValue());
  }

}
