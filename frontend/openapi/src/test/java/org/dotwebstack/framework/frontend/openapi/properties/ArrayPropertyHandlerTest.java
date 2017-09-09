package org.dotwebstack.framework.frontend.openapi.properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import io.swagger.models.properties.ArrayProperty;
import org.eclipse.rdf4j.model.Value;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ArrayPropertyHandlerTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private ArrayPropertyHandler handler;

  private ArrayProperty property;

  @Before
  public void setUp() {
    handler = new ArrayPropertyHandler();
    property = new ArrayProperty();
  }

  @Test
  public void supportsArrayProperty() {
    assertThat(handler.supports(property), equalTo(true));
  }

  @Test
  public void handleTupleResult() {
    // Assert
    thrown.expect(PropertyHandlerRuntimeException.class);
    thrown.expectMessage("Array property is not supported for tuple results.");

    // Act
    handler.handle(property, mock(Value.class));
  }

}
