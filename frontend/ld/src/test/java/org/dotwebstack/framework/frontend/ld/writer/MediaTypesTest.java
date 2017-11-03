package org.dotwebstack.framework.frontend.ld.writer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.dotwebstack.framework.frontend.ld.MediaTypes;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MediaTypesTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void constructor_CannotBeInstantiated_BecauseAccessPrivate() throws NoSuchMethodException {
    // Arrange & Act
    Constructor<MediaTypes> constructor = MediaTypes.class.getDeclaredConstructor();

    // Assert
    assertThat(Modifier.isPrivate(constructor.getModifiers()), equalTo(true));
  }

  @Test
  public void constructor_ThrowsException_TryingToInstantiate() throws NoSuchMethodException,
      IllegalAccessException, InvocationTargetException, InstantiationException {
    // Arrange
    Constructor<MediaTypes> constructor = MediaTypes.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    // Assert
    thrown.expect(InvocationTargetException.class);
    String expectedMessage =
        "class org.dotwebstack.framework.frontend.ld.MediaTypes is not meant to be instantiated.";
    thrown.expectCause(hasProperty("message", equalTo(expectedMessage)));

    // Act
    constructor.newInstance();
  }
}
