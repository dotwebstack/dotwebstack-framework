package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class XHTML {

  public static final IRI STYLESHEET;

  private static final String NAMESPACE_BASE = "http://www.w3.org/1999/xhtml/vocab";

  private static final String NAMESPACE = NAMESPACE_BASE + "#";

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    STYLESHEET = valueFactory.createIRI(XHTML.NAMESPACE, "stylesheet");
  }

  private XHTML() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", XHTML.class));
  }

}
