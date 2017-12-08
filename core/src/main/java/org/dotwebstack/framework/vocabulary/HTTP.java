package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class HTTP {

  private static final String NAMESPACE_BASE = "http://www.w3.org/2011/http";

  private static final String NAMESPACE = NAMESPACE_BASE + "#";

  public static final IRI REQUEST_URI;

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    REQUEST_URI = valueFactory.createIRI(HTTP.NAMESPACE, "requestURI");
  }

  private HTTP() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", HTTP.class));
  }

}
