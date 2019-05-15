package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import org.eclipse.rdf4j.model.vocabulary.RDF;

public class PropertyPathHelper {

  private PropertyPathHelper() {}

  public static boolean isNil(PropertyPath propertyPath) {
    return propertyPath instanceof PredicatePath && RDF.NIL.equals(((PredicatePath) propertyPath).getIri());
  }
}
