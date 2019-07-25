package org.dotwebstack.framework.backend.rdf4j.helper;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class IriHelper {

  public static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

  private IriHelper() { }

  public static IRI createIri(String iriString) {
    return VF.createIRI(iriString);
  }

  public static String stringify(IRI iri) {
    return "<" + iri.stringValue() + ">";
  }

}
