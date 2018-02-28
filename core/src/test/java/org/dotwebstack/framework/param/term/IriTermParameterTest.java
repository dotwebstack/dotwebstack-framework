package org.dotwebstack.framework.param.term;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.BindableParameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IriTermParameterTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private BindableParameter<IRI> requiredParameter;
  private BindableParameter<IRI> optionalParameter;

  @Before
  public void setUp() {
    requiredParameter = new IriTermParameter(DBEERPEDIA.NAME_PARAMETER_ID,
        DBEERPEDIA.NAME_PARAMETER_VALUE_STRING, true);
    optionalParameter = new IriTermParameter(DBEERPEDIA.PLACE_PARAMETER_ID,
        DBEERPEDIA.PLACE_PARAMETER_VALUE_STRING, false);
  }

  @Test
  public void handle_ReturnsNonNullValue_ForRequiredParameter() {
    // Arrange
    Map<String, String> parameterValues =
        ImmutableMap.of(DBEERPEDIA.NAME_PARAMETER_VALUE_STRING, "http://iri");

    // Act
    IRI result = requiredParameter.handle(parameterValues);

    // Assert
    assertThat(result, is(VALUE_FACTORY.createIRI("http://iri")));
  }

  @Test
  public void handle_ReturnsNull_ForOptionalParameter() {
    // Arrange
    Map<String, String> parameterValues =
        Collections.singletonMap(DBEERPEDIA.PLACE_PARAMETER_VALUE_STRING, null);

    // Act
    IRI result = optionalParameter.handle(parameterValues);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void handle_ReturnsDefaultValue_ForOptionalParameterWithNullInput() {
    // Arrange
    IRI defaultValue = SimpleValueFactory.getInstance().createIRI("http://defaultValue");

    BindableParameter<IRI> parameter = new IriTermParameter(DBEERPEDIA.PLACE_PARAMETER_ID,
        DBEERPEDIA.PLACE_PARAMETER_VALUE_STRING, false, defaultValue);

    Map<String, String> parameterValues =
        Collections.singletonMap(DBEERPEDIA.PLACE_PARAMETER_VALUE_STRING, null);

    // Act
    IRI result = parameter.handle(parameterValues);

    // Assert
    assertThat(result, is(defaultValue));
  }

  @Test
  public void handle_RejectsNullValue_ForRequiredParameter() {
    // Assert
    thrown.expect(BackendException.class);
    thrown.expectMessage(
        String.format("No value found for required parameter '%s'. Supplied parameterValues:",
            DBEERPEDIA.NAME_PARAMETER_ID));

    // Act
    IRI handle = requiredParameter.handle(ImmutableMap.of());

    assertThat(handle, is("x"));
  }

  @Test
  public void getValue_ReturnsLiteral_ForValue() {
    // Arrange
    Map<String, String> parameterValues =
        ImmutableMap.of(DBEERPEDIA.NAME_PARAMETER_VALUE_STRING, "http://iri");

    // Act
    IRI value = requiredParameter.handle(parameterValues);
    Value result = requiredParameter.getValue(value);

    // Assert
    assertThat(result, is(SimpleValueFactory.getInstance().createIRI("http://iri")));
  }

}
