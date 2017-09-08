package org.dotwebstack.framework.frontend.openapi.properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.Property;
import org.eclipse.rdf4j.model.Value;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PropertyHandlerAdapterTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private PropertyHandler<? extends Property, Object> propertyHandler;

  @Mock
  private Property property;

  @Mock
  private Value value;

  private PropertyHandlerAdapter propertyHandlerAdapter;

  @Test
  public void handleWithoutHandlers() {
    // Arrange
    propertyHandlerAdapter = new PropertyHandlerAdapter(ImmutableList.of());

    // Arrange
    thrown.expect(PropertyHandlerRuntimeException.class);
    thrown.expectMessage(
        String.format("No property handler available for '%s'.", property.getClass().getName()));

    // Act
    propertyHandlerAdapter.handle(property, value);
  }

  @Test
  public void handleWithoutSupportingHandlers() {
    // Arrange
    propertyHandlerAdapter = new PropertyHandlerAdapter(ImmutableList.of(propertyHandler));
    when(propertyHandler.supports(property)).thenReturn(false);

    // Arrange
    thrown.expect(PropertyHandlerRuntimeException.class);
    thrown.expectMessage(
        String.format("No property handler available for '%s'.", property.getClass().getName()));

    // Act
    propertyHandlerAdapter.handle(property, value);
  }

  @Test
  public void handleWithSupportingHandler() {
    // Arrange
    propertyHandlerAdapter = new PropertyHandlerAdapter(ImmutableList.of(propertyHandler));
    when(propertyHandler.supports(property)).thenReturn(true);
    Object expectedResult = new Object();
    when(propertyHandler.handle(any(), eq(value))).thenReturn(expectedResult);

    // Act
    Object result = propertyHandlerAdapter.handle(property, value);

    // Arrange
    assertThat(result, equalTo(expectedResult));
  }

}
