package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import javax.ws.rs.core.MediaType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntityTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testGetSchema() {
    // Arrange
    Property expectedSchema = mock(Property.class);
    Entity entity =
        new Entity(new Object(), ImmutableMap.of(MediaType.APPLICATION_JSON, expectedSchema));

    // Act
    Property actualSchema = entity.getSchema(MediaType.APPLICATION_JSON);

    // Assert
    assertThat(actualSchema, equalTo(expectedSchema));
  }

  @Test
  public void testGetUnknownSchema() {
    // Arrange
    Property expectedSchema = mock(Property.class);
    Entity entity =
        new Entity(new Object(), ImmutableMap.of(MediaType.APPLICATION_JSON, expectedSchema));

    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    entity.getSchema(MediaType.TEXT_PLAIN);
  }

}
