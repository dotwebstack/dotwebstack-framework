package org.dotwebstack.framework.param.types;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class IntTermParameterTest {

  private IRI identifier = SimpleValueFactory.getInstance().createIRI("http://www.test.nl");
  private IntTermParameter intTermParameter = new IntTermParameter(identifier, "test", true);


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void parseValue() throws Exception {
    // Arrange
    Map<String, String> parameterValues = new HashMap<>();
    parameterValues.put("test", "123");
    // Act
    Integer result = intTermParameter.parseValue(parameterValues);
    // Assert
    assertEquals(123, result.intValue());
  }

  @Test
  public void validateRequired() throws Exception {
    // Arrange
    Map<String, String> parameterValues = new HashMap<>();
    parameterValues.put("test", "12f3");

    // Assert
    thrown.expect(BackendException.class);
    // Act
    intTermParameter.validateRequired(parameterValues);


  }

}
