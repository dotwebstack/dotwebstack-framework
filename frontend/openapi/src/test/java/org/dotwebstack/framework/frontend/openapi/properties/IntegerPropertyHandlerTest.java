package org.dotwebstack.framework.frontend.openapi.properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.IntegerProperty;
import java.math.BigInteger;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IntegerPropertyHandlerTest {

  private final static Literal INTEGER_LITERAL = SimpleValueFactory.getInstance().createLiteral(3);

  private final static Literal STRING_LITERAL = SimpleValueFactory.getInstance().createLiteral("3");

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private IntegerPropertyHandler handler;

  private IntegerProperty property;

  @Before
  public void setUp() {
    handler = new IntegerPropertyHandler();
    property = new IntegerProperty();
  }

  @Test
  public void supportsIntegerProperty() {
    assertThat(handler.supports(property), equalTo(true));
  }

  @Test
  public void handleTupleResultIntegerLiteral() {
    // Act
    BigInteger result = handler.handle(property, INTEGER_LITERAL);

    // Assert
    assertThat(result, equalTo(BigInteger.valueOf(3)));
  }

  @Test
  public void handleTupleResultStringLiteral() {
    // Act
    BigInteger result = handler.handle(property, STRING_LITERAL);

    // Assert
    assertThat(result, equalTo(BigInteger.valueOf(3)));
  }

}
