package org.dotwebstack.framework.frontend.openapi.properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.StringProperty;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringPropertyHandlerTest {

  private final static Literal STRING_LITERAL =
      SimpleValueFactory.getInstance().createLiteral("Three");

  private final static Literal INTEGER_LITERAL = SimpleValueFactory.getInstance().createLiteral(3);

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private StringPropertyHandler handler;

  private StringProperty property;

  @Before
  public void setUp() {
    handler = new StringPropertyHandler();
    property = new StringProperty();
  }

  @Test
  public void supportsStringProperty() {
    assertThat(handler.supports(property), equalTo(true));
  }

  @Test
  public void handleTupleResultStringLiteral() {
    // Act
    String result = handler.handle(property, STRING_LITERAL);

    // Assert
    assertThat(result, equalTo("Three"));
  }

  @Test
  public void handleTupleResultIntegerLiteral() {
    // Act
    String result = handler.handle(property, INTEGER_LITERAL);

    // Assert
    assertThat(result, equalTo("3"));
  }

}
