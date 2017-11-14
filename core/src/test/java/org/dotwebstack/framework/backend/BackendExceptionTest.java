package org.dotwebstack.framework.backend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BackendExceptionTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void constructor_Initiates_WithMessage() {
    // Arrange & Act
    BackendException exception = new BackendException("myMessage");

    // Assert
    assertThat("myMessage", equalTo(exception.getMessage()));
  }

  @Test
  public void constructor_Initiates_WithMessageAndCause() {
    // Arrange
    NullPointerException cause = new NullPointerException();

    // Act
    BackendException exception = new BackendException("myMessage", cause);

    // Assert
    assertThat("myMessage", equalTo(exception.getMessage()));
    assertThat(cause, equalTo(exception.getCause()));
  }

  @Test
  public void throw_ThrowException_WithMessage() {
    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage("myMessage");

    // Act
    throw new BackendException("myMessage");
  }
}
