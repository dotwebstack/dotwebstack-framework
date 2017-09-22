package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import javax.ws.rs.core.MediaType;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TupleEntityTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void constructor_ThrowsException_WithMissingSchemaMap() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TupleEntity(null, mock(TupleQueryResult.class));
  }

  @Test
  public void constructor_ThrowsException_WithMissingResult() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, mock(Property.class)), null);
  }

  @Test
  public void getSchema_GivesSchema_ForJSON() {
    // Arrange
    Property expectedSchema = mock(Property.class);
    TupleEntity entity =
        new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, expectedSchema),
            mock(TupleQueryResult.class));

    // Act
    Property actualSchema = entity.getSchema(MediaType.APPLICATION_JSON_TYPE);

    // Assert
    assertThat(actualSchema, equalTo(expectedSchema));
  }

  @Test
  public void getSchema_ThrowsException_ForUnknownMediaType() {
    // Arrange
    Property expectedSchema = mock(Property.class);
    TupleEntity entity =
        new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, expectedSchema),
            mock(TupleQueryResult.class));

    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    entity.getSchema(MediaType.TEXT_PLAIN_TYPE);
  }

}
