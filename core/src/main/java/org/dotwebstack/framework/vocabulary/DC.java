package org.dotwebstack.framework.vocabulary;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class DC {

  private static final String NAMESPACE_BASE = "<http://purl.org/dc/elements/1.1>";
  private static final String NAMESPACE = NAMESPACE_BASE + "/";

  public static final IRI TITLE;

  static {
    ValueFactory valueFactory = SimpleValueFactory.getInstance();

    TITLE = valueFactory.createIRI(DC.NAMESPACE, "title");
  }

  private DC() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", DC.class));
  }

}
