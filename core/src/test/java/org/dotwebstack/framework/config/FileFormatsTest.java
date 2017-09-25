package org.dotwebstack.framework.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FileFormatsTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void constructor_CannotBeInstantiated_BecauseAccessPrivate() throws NoSuchMethodException {
    // Arrange & Act
    Constructor<FileFormats> constructor = FileFormats.class.getDeclaredConstructor();

    // Assert
    assertThat(Modifier.isPrivate(constructor.getModifiers()), equalTo(true));
  }

  @Test
  public void constructor_ThrowsException_TryingToInstantiate() throws NoSuchMethodException,
      IllegalAccessException, InvocationTargetException, InstantiationException {
    // Arrange
    Constructor<FileFormats> constructor = FileFormats.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    // Assert
    thrown.expect(InvocationTargetException.class);
    thrown.expectCause(hasProperty("message", equalTo(
        "class org.dotwebstack.framework.config.FileFormats is not meant to be instantiated.")));

    // Act
    constructor.newInstance();
  }
}
