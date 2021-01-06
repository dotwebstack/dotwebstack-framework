package org.dotwebstack.framework.backend.rdf4j.converters;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.BooleanLiteral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class Rdf4jConverterRouterTest {

  private Rdf4jConverterRouter rdf4jConverterRouter;

  @BeforeEach
  void setUp() {
    rdf4jConverterRouter = new Rdf4jConverterRouter(List.of(new BooleanConverter()));
  }

  @Test
  void testConvertFromValueSuccess() {
    // Arrange
    BooleanLiteral boolLiteral = BooleanLiteral.TRUE;

    // Act
    Object result = rdf4jConverterRouter.convertFromValue(boolLiteral);

    // Assert
    assertTrue((Boolean) result);
  }

  @Test
  void testConvertToValueSuccess() {
    // Arrange / Act
    Literal result = (Literal) rdf4jConverterRouter.convertToValue(true, "Boolean");

    // Assert
    assertTrue(result.booleanValue());
  }

  @Test
  void testConvertToValueThrowsException() {
    // Arrange / Act / Assert
    assertThrows(InvalidConfigurationException.class,
        () -> rdf4jConverterRouter.convertToValue("someDate", "LocateDateTime"));
  }

}
