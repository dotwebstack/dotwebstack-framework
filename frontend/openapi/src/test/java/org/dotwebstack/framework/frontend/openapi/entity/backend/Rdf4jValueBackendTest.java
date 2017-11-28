package org.dotwebstack.framework.frontend.openapi.entity.backend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Locale;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Test;

public class Rdf4jValueBackendTest {

  @Test
  public void testGetLiteralLocale() {
    Literal value = SimpleValueFactory.getInstance().createLiteral("value", "nl");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    Locale locale = backend.getLiteralLanguage(value);
    assertThat("Locale is incorrect", locale, is(Locale.forLanguageTag("nl")));
  }

  @Test
  public void testGetLiteralType() {
    Literal value = SimpleValueFactory.getInstance().createLiteral("value");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat("Literal type is incorrect", backend.getLiteralType(value).toString(),
        is(XMLSchema.STRING.stringValue()));
  }

  @Test
  public void testGetDecimalType() {
    Literal value = SimpleValueFactory.getInstance().createLiteral(BigDecimal.ONE);

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.decimalValue(value).toString(), is("1"));
  }

  @Test
  public void testcreateLiteral() {
    URI uri = URI.create("http://www.test.nl");
    Literal value = SimpleValueFactory.getInstance().createLiteral("test",
        SimpleValueFactory.getInstance().createIRI(uri.toString()));

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.createLiteral("test", Locale.CANADA, uri), is(value));
  }
  @Test
  public void testcreateLiteralLanguage() {
    URI uri = URI.create("http://www.test.nl");
    Literal value = SimpleValueFactory.getInstance().createLiteral("test","en");

    Rdf4jValueBackend backend = new Rdf4jValueBackend();

    assertThat(backend.createLiteral("test", Locale.ENGLISH,null), is(value));
  }

}
