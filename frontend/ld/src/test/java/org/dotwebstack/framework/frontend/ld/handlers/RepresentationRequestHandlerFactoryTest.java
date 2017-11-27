package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RepresentationRequestHandlerFactoryTest {

  RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  @Mock
  Representation representation;

  @Mock
  RepresentationRequestParameterMapper representationRequestParameterMapper;

  @Before
  public void setUp() {
    representationRequestHandlerFactory =
        new RepresentationRequestHandlerFactory(representationRequestParameterMapper);
  }

  @Test
  public void newRepresentationRequestHandler_createsRepresentationRequestHandler_WithValidData() {
    // Arrange

    // Act
    RepresentationRequestHandler representationRequestHandler =
        representationRequestHandlerFactory.newRepresentationRequestHandler(representation);

    // Assert
    assertThat(representationRequestHandler.getRepresentationRequestParameterMapper(),
        sameInstance(representationRequestParameterMapper));
  }

}
