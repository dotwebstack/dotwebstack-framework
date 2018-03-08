package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.frontend.ld.endpoint.EndPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EndPointRequestHandlerFactoryTest {

  EndPointRequestHandlerFactory representationRequestHandlerFactory;

  @Mock
  EndPoint endPoint;

  @Mock
  EndPointRequestParameterMapper representationRequestParameterMapper;

  @Before
  public void setUp() {
    representationRequestHandlerFactory =
        new EndPointRequestHandlerFactory(representationRequestParameterMapper);
  }

  @Test
  public void newRepresentationRequestHandler_createsRepresentationRequestHandler_WithValidData() {
    // Arrange

    // Act
    EndPointRequestHandler endPointRequestHandler =
        representationRequestHandlerFactory.newRepresentationRequestHandler(endPoint);

    // Assert
    assertThat(endPointRequestHandler.getEndPointRequestParameterMapper(),
        sameInstance(representationRequestParameterMapper));
  }

}
