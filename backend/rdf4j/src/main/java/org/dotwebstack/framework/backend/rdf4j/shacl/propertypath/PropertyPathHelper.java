package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import org.eclipse.rdf4j.model.vocabulary.RDF;

public class PropertyPathHelper {

  private PropertyPathHelper() {}

  public static boolean isNil(PredicatePath predicatePath) {
    return RDF.NIL.equals(predicatePath.getIri());
  }
}
