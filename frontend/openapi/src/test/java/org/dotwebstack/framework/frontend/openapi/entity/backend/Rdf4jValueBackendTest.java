package org.dotwebstack.framework.frontend.openapi.entity.backend;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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

}
