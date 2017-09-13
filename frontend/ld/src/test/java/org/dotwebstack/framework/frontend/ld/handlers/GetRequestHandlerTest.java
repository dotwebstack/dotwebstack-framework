package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetRequestHandlerTest {

  @Mock
  private Representation representation;

  @Mock
  private ContainerRequestContext containerRequestContext;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private GraphQueryResult queryResult;

  private GetRequestHandler getRequestHandler;

  @Before
  public void setUp() {
    getRequestHandler = new GetRequestHandler(representation);
  }

  @Test
  public void alwaysReturnRepresentationInformationProductResult() {
    // Arrange
    when(representation.getInformationProduct()).thenReturn(informationProduct);
    when(informationProduct.getResult()).thenReturn(queryResult);

    UriInfo uriInfo = mock(UriInfo.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/");

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), equalTo(queryResult));
  }
}
