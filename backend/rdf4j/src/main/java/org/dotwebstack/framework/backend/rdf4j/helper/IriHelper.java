package org.dotwebstack.framework.backend.rdf4j.helper;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class IriHelper {

  private static final SimpleValueFactory VF = SimpleValueFactory.getInstance();

  private IriHelper() {}

  public static IRI createIri(@NonNull String iriString) {
    return VF.createIRI(iriString);
  }

  public static String stringify(@NonNull IRI iri) {
    return "<" + iri.stringValue() + ">";
  }

}
