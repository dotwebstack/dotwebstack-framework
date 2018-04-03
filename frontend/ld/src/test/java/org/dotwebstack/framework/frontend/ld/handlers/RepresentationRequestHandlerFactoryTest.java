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
public class RepresentationRequestHandlerFactoryTest {

  RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  @Mock
  AbstractEndPoint endPoint;

  @Mock
  EndPointRequestParameterMapper endPointRequestParameterMapper;

  @Mock
  RepresentationResourceProvider representationResourceProvider;

  @Before
  public void setUp() {
    representationRequestHandlerFactory = new RepresentationRequestHandlerFactory(
        endPointRequestParameterMapper, representationResourceProvider);
  }

  @Test
  public void newRepresentationRequestHandler_createsRepresentationRequestHandler_WithValidData() {
    // Arrange

    // Act
    RepresentationRequestHandler representationRequestHandler =
        representationRequestHandlerFactory.newRepresentationRequestHandler(endPoint);

    // Assert
    assertThat(representationRequestHandler.getEndPointRequestParameterMapper(),
        sameInstance(endPointRequestParameterMapper));
  }

}
