package org.dotwebstack.framework.frontend.openapi.properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import io.swagger.models.properties.ObjectProperty;
import org.eclipse.rdf4j.model.Value;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ObjectPropertyHandlerTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private ObjectPropertyHandler handler;

  private ObjectProperty property;

  @Before
  public void setUp() {
    handler = new ObjectPropertyHandler();
    property = new ObjectProperty();
  }

  @Test
  public void supportsObjectProperty() {
    assertThat(handler.supports(property), equalTo(true));
  }

  @Test
  public void handleTupleResult() {
    // Assert
    thrown.expect(PropertyHandlerRuntimeException.class);
    thrown.expectMessage("Object property is not supported for tuple results.");

    // Act
    handler.handle(property, mock(Value.class));
  }

}
