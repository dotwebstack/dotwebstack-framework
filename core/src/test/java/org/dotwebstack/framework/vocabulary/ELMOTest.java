package org.dotwebstack.framework.vocabulary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.IsEqual.equalTo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ELMOTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void constructor_CannotBeInstantiated_BecauseAccessPrivate() throws NoSuchMethodException {
    // Arrange & Act
    Constructor<ELMO> constructor = ELMO.class.getDeclaredConstructor();

    // Assert
    assertThat(Modifier.isPrivate(constructor.getModifiers()), equalTo(true));
  }

  @Test
  public void constructor_ThrowsException_TryingToInstantiate() throws NoSuchMethodException,
      IllegalAccessException, InvocationTargetException, InstantiationException {
    // Arrange
    Constructor<ELMO> constructor = ELMO.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    // Assert
    thrown.expect(InvocationTargetException.class);
    thrown.expectCause(hasProperty("message", equalTo(
        "class org.dotwebstack.framework.vocabulary.ELMO is not meant to be instantiated.")));

    // Act
    constructor.newInstance();
  }
}
