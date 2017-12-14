package org.dotwebstack.framework.frontend.openapi.entity.backend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Rdf4jValueBackendTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void getLiteralLanguage_RetursLanguage_ForLiteral() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral("value", "nl");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    Locale locale = backend.getLiteralLanguage(value);
    // Assert
    assertThat("Locale is incorrect", locale, is(Locale.forLanguageTag("nl")));
  }

  @Test
  public void getLiteralType_ReturnsType_ForLiteral() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral("value");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    URI result = backend.getLiteralType(value);

    // Assert
    assertThat("Literal type is incorrect", result.toString(), is(XMLSchema.STRING.stringValue()));
  }

  @Test
  public void decimalValue_ReturnsDecimalValue_ForLiteral() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral(BigDecimal.ONE);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    BigDecimal result = backend.decimalValue(value);

    // Assert
    assertThat(result.toString(), is("1"));
  }

  @Test
  public void createLiteral_ReturnsLiteral_ForContentLocaleAndUri() {
    // Arrange
    URI uri = URI.create("http://www.test.nl");
    Literal value = SimpleValueFactory.getInstance().createLiteral("test",
        SimpleValueFactory.getInstance().createIRI(uri.toString()));

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    Literal result = backend.createLiteral("test", Locale.CANADA, uri);

    // Assert
    assertThat(result, is(value));
  }

  @Test
  public void createLiteral_ReturnsLiteral_ForContentAndLocale() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral("test", "en");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    Literal result = backend.createLiteral("test", Locale.ENGLISH, null);

    // Assert
    assertThat(result, is(value));
  }

  @Test
  public void getLiteralType_ThrowsException_ForIllegalLiteral() {
    // Assert
    expectedException.expect(IllegalArgumentException.class);

    // Arrange
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    backend.getLiteralType(value);
  }

  @Test
  public void booleanValue_ReturnsBooleanValue_ForLiteral() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral(Boolean.TRUE);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    Boolean result = backend.booleanValue(value);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void integerValue_ReturnsIntegerValue_ForLiteral() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral(10);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    BigInteger result = backend.integerValue(value);

    // Assert
    assertThat(result, is(BigInteger.TEN));
  }

  @Test
  public void floatValue_ReturnsFloatValue_ForLiteral() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral(10.3F);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    Float result = backend.floatValue(value);

    // Assert
    assertThat(result, is(10.3F));
  }

  @Test
  public void intValue_ReturnsIntValue_ForLiteral() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral(10);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    Integer result = backend.intValue(value);

    // Assert
    assertThat(result, is(10));
  }

  @Test
  public void createLiteral_ReturnsLiteral_ForContent() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral("http://www.test.nl");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    Literal result = backend.createLiteral("http://www.test.nl");

    // Assert
    assertThat(result, is(value));
  }

  @Test
  public void doubleValue_ReturnsDoubleValue_ForLiteral() {
    // Arrange
    Literal value = SimpleValueFactory.getInstance().createLiteral(10.0);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    Double result = backend.doubleValue(value);

    // Assert
    assertThat(result, is(10.0));
  }

  private XMLGregorianCalendar getCurrentDateTime() {
    GregorianCalendar gc = new GregorianCalendar();
    DatatypeFactory dtf = null;
    try {
      dtf = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException ex) {
      ex.printStackTrace();
    }
    return dtf.newXMLGregorianCalendar(gc);
  }

  @Test
  public void dateTimeValue_ReturnsDateTimeValue_ForLiteral() {
    // Arrange
    XMLGregorianCalendar currentTime = getCurrentDateTime();
    Literal value = SimpleValueFactory.getInstance().createLiteral(currentTime);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    Date result = backend.dateTimeValue(value);

    // Assert
    assertThat(result.getTime(), is(currentTime.toGregorianCalendar().getTime().getTime()));
  }

  @Test
  public void doubleValue_ThrowsException_ForIllegalDoubleValue() {
    // Assert
    expectedException.expect(IllegalArgumentException.class);

    // Arrange
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    backend.doubleValue(value);
  }

  @Test
  public void integerValue_ThrowsException_ForIllegalIntegerValue() {
    // Assert
    expectedException.expect(IllegalArgumentException.class);

    // Arrange
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    backend.integerValue(value);
  }

  @Test
  public void dateValue_ThrowsException_ForIllegalDateValue() {
    // Assert
    expectedException.expect(IllegalArgumentException.class);

    // Arrange
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    backend.dateValue(value);
  }

  @Test
  public void dateTimeValue_ThrowsException_ForIllegalDateTimeValue() {
    // Assert
    expectedException.expect(IllegalArgumentException.class);

    // Arrange
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    backend.dateTimeValue(value);
  }

  @Test
  public void intValue_ThrowsException_ForIllegalIntValue() {
    // Assert
    expectedException.expect(IllegalArgumentException.class);

    // Arrange
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    backend.intValue(value);
  }

  @Test
  public void floatValue_ThrowsException_ForIllegalFloatValue() {
    // Assert
    expectedException.expect(IllegalArgumentException.class);

    // Arrange
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    // Act
    backend.floatValue(value);
  }

}
