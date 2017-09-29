package org.dotwebstack.framework.frontend.openapi.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SchemaMapperUtilsTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void constructor_CannotBeInstantiated_BecauseAccessPrivate() throws NoSuchMethodException {
    // Arrange & Act
    Constructor<SchemaMapperUtils> constructor = SchemaMapperUtils.class.getDeclaredConstructor();

    // Assert
    assertThat(Modifier.isPrivate(constructor.getModifiers()), equalTo(true));
  }

  @Test
  public void constructor_ThrowsException_TryingToInstantiate() throws NoSuchMethodException,
      IllegalAccessException, InvocationTargetException, InstantiationException {
    // Arrange
    Constructor<SchemaMapperUtils> constructor = SchemaMapperUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    // Assert
    thrown.expect(InvocationTargetException.class);
    thrown.expectCause(
        hasProperty("message", equalTo(String.format("class %s is not meant to be instantiated.",
            SchemaMapperUtils.class.getName()))));

    // Act
    constructor.newInstance();
  }

  @Test
  public void castLiteralValue_ThrowsException_ForNonLiteralValue() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Act
    SchemaMapperUtils.castLiteralValue(DBEERPEDIA.BROUWTOREN);
  }

  @Test
  public void castLiteralValue_ReturnsLiteral_ForLiteralValue() {
    // Act
    Literal result = SchemaMapperUtils.castLiteralValue(DBEERPEDIA.BROUWTOREN_NAME);

    // Assert
    assertThat(result, notNullValue());
    assertThat(result.getDatatype(), equalTo(XMLSchema.STRING));
  }

}
