package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.frontend.ld.parameter.QueryParameterMapper;
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
  QueryParameterMapper queryParameterMapper;

  @Before
  public void setUp() {
    representationRequestHandlerFactory =
        new RepresentationRequestHandlerFactory(queryParameterMapper);
  }

  @Test
  public void newRepresentationRequestHandler_createsRepresentationRequestHandler_WithValidData() {
    // Arrange

    // Act
    RepresentationRequestHandler representationRequestHandler =
        representationRequestHandlerFactory.newRepresentationRequestHandler(representation);

    // Assert
    assertThat(representationRequestHandler.getQueryParameterMapper(),
        sameInstance(queryParameterMapper));
  }

}
