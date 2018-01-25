package org.dotwebstack.framework.param.types;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.BindableParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IntTermParameterTest {

  private IRI identifier = SimpleValueFactory.getInstance().createIRI("http://www.test.nl");
  private IntTermParameter intTermParameter = new IntTermParameter(identifier, "test", true);
  private IntTermParameter intTermParameterOptional =
      new IntTermParameter(identifier, "test", false);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void handle_ReturnsNull_ForOptionalParameter() {
    // Arrange
    Map<String, String> parameterValues = Collections.singletonMap("test", null);

    // Act
    Integer result = intTermParameterOptional.handle(parameterValues);

    // Assert
    assertNull(result);
  }

  @Test
  public void handle_ReturnsDefaultValue_ForOptionalParameterWithNullInput() {
    // Arrange
    Integer defaultValue = 3;

    BindableParameter<Integer> parameter =
        new IntTermParameter(identifier, "test", false, defaultValue);

    Map<String, String> parameterValues = Collections.singletonMap("test", null);

    // Act
    Integer result = parameter.handle(parameterValues);

    // Assert
    assertThat(result, is(defaultValue));
  }

  @Test
  public void handle_ReturnsNonNullValue_ForRequiredParameter() {
    // Arrange
    Map<String, String> parameterValues = ImmutableMap.of("test", "123");

    // Act
    Integer result = intTermParameter.handle(parameterValues);

    // Assert
    assertThat(result, is(123));
  }

  @Test
  public void handle_RejectsNullValue_ForRequiredParameter() {
    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(String.format(
        "No value found for required parameter '%s'. Supplied parameterValues:", identifier));

    // Act
    intTermParameter.handle(ImmutableMap.of());
  }

  @Test
  public void getValue_ReturnsLiteral_ForValue() {
    // Arrange
    Map<String, String> parameterValues = ImmutableMap.of("test", "123");

    // Act
    Integer value = intTermParameter.handle(parameterValues);
    Value result = intTermParameter.getValue(value);

    // Assert
    assertThat(result, is(SimpleValueFactory.getInstance().createLiteral(123)));
  }

}
