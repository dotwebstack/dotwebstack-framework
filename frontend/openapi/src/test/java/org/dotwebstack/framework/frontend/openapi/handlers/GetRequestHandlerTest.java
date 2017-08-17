package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetRequestHandlerTest {

  @Mock
  InformationProduct informationProduct;

  @Mock
  ContainerRequestContext containerRequestContext;

  GetRequestHandler getRequestHandler;

  @Before
  public void setUp() {
    getRequestHandler = new GetRequestHandler(informationProduct);
  }

  @Test
  public void alwaysReturnInformationProductIdentifier() {
    // Arrange
    when(informationProduct.getIdentifier()).thenReturn(DBEERPEDIA.BREWERIES);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), equalTo(DBEERPEDIA.BREWERIES.stringValue()));
  }

}
