package org.dotwebstack.framework.frontend.openapi.entity.backend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
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
  public void testGetLiteralLanguage() {
    Literal value = SimpleValueFactory.getInstance().createLiteral("value", "nl");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    Locale locale = backend.getLiteralLanguage(value);
    assertThat("Locale is incorrect", locale, is(Locale.forLanguageTag("nl")));
  }

  @Test
  public void testGetLiteralStringType() {
    Literal value = SimpleValueFactory.getInstance().createLiteral("value");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat("Literal type is incorrect", backend.getLiteralType(value).toString(),
        is(XMLSchema.STRING.stringValue()));
  }

  @Test
  public void testGetDecimalValueType() {
    Literal value = SimpleValueFactory.getInstance().createLiteral(BigDecimal.ONE);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.decimalValue(value).toString(), is("1"));
  }

  @Test
  public void testCreateLiteralWithLocaleAndIri() {
    URI uri = URI.create("http://www.test.nl");
    Literal value = SimpleValueFactory.getInstance().createLiteral("test",
        SimpleValueFactory.getInstance().createIRI(uri.toString()));

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.createLiteral("test", Locale.CANADA, uri), is(value));
  }

  @Test
  public void testCreateLiteralLanguageWithNoUri() {
    Literal value = SimpleValueFactory.getInstance().createLiteral("test", "en");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.createLiteral("test", Locale.ENGLISH, null), is(value));
  }

  @Test
  public void testCreateLiteralClassCast() {
    expectedException.expect(IllegalArgumentException.class);
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.getLiteralType(value), is(value));
  }

  @Test
  public void testGetBooleanType() {
    Literal value = SimpleValueFactory.getInstance().createLiteral(Boolean.TRUE);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.booleanValue(value), is(true));
  }

  @Test
  public void testGetIntegerType() {
    Literal value = SimpleValueFactory.getInstance().createLiteral(10);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.integerValue(value), is(BigInteger.TEN));
  }

  @Test
  public void testGetFloatType() {
    Literal value = SimpleValueFactory.getInstance().createLiteral(10.3F);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.floatValue(value), is(10.3F));
  }

  @Test
  public void testGetIntType() {
    Literal value = SimpleValueFactory.getInstance().createLiteral(10);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.intValue(value), is(10));
  }

  @Test
  public void testCreateLiteral() {
    Literal value = SimpleValueFactory.getInstance().createLiteral("http://www.test.nl");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.createLiteral("http://www.test.nl"), is(value));
  }

  @Test
  public void testGetDoubleType() {
    Literal value = SimpleValueFactory.getInstance().createLiteral(10.0);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.doubleValue(value), is(10.0));
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
  public void testGetDate() {
    XMLGregorianCalendar currentTime = getCurrentDateTime();
    Literal value = SimpleValueFactory.getInstance().createLiteral(currentTime);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.dateTimeValue(value).getTime(),
        is(currentTime.toGregorianCalendar().getTime().getTime()));
  }

  @Test
  public void testGetClassCastExceptionDouble() {
    expectedException.expect(IllegalArgumentException.class);
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    backend.doubleValue(value);
  }

  @Test
  public void testGetClassCastExceptionInteger() {
    expectedException.expect(IllegalArgumentException.class);
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    backend.integerValue(value);
  }

  @Test
  public void testGetClassCastExceptionDate() {
    expectedException.expect(IllegalArgumentException.class);
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    backend.dateValue(value);
  }

  @Test
  public void testGetClassCastExceptionDateTime() {
    expectedException.expect(IllegalArgumentException.class);
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    backend.dateTimeValue(value);
  }

  @Test
  public void testGetClassCastExceptionInt() {
    expectedException.expect(IllegalArgumentException.class);
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    backend.intValue(value);
  }

  @Test
  public void testGetClassCastExceptionFloat() {
    expectedException.expect(IllegalArgumentException.class);
    BNode value = SimpleValueFactory.getInstance().createBNode("test");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    backend.floatValue(value);
  }

}
