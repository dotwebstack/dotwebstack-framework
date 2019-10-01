package org.dotwebstack.framework.backend.rdf4j.constants;

import org.dotwebstack.framework.backend.rdf4j.helper.IriHelper;
import org.eclipse.rdf4j.model.IRI;

public class Rdf4jConstants {

  private Rdf4jConstants() {}

  public static final String DOTWEBSTACK_NAMESPACE = "http://www.dotwebstack.org/";

  public static final IRI DOTWEBSTACK_INHERITS = IriHelper.createIri(DOTWEBSTACK_NAMESPACE + "inherits");

}
