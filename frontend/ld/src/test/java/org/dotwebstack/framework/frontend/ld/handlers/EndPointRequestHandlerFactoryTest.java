package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.frontend.ld.endpoint.AbstractEndPoint;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EndPointRequestHandlerFactoryTest {

  EndPointRequestHandlerFactory endPointRequestHandlerFactory;

  @Mock
  AbstractEndPoint endPoint;

  @Mock
  EndPointRequestParameterMapper endPointRequestParameterMapper;

  @Mock
  RepresentationResourceProvider representationResourceProvider;

  @Before
  public void setUp() {
    endPointRequestHandlerFactory = new EndPointRequestHandlerFactory(
        endPointRequestParameterMapper, representationResourceProvider);
  }

  @Test
  public void newRepresentationRequestHandler_createsRepresentationRequestHandler_WithValidData() {
    // Arrange

    // Act
    EndPointRequestHandler endPointRequestHandler =
        endPointRequestHandlerFactory.newEndPointRequestHandler(endPoint);

    // Assert
    assertThat(endPointRequestHandler.getEndPointRequestParameterMapper(),
        sameInstance(endPointRequestParameterMapper));
  }

}
