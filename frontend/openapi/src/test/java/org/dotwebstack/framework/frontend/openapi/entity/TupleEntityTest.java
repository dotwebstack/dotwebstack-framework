package org.dotwebstack.framework.frontend.openapi.entity;

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
    new TupleEntity(null, null, null, null, null);
  }

  @Test
  public void constructor_ThrowsException_WithMissingResult() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TupleEntity(null, null, null, null, null);
  }

}
