package org.dotwebstack.framework.param;

import org.eclipse.rdf4j.model.IRI;

public interface Parameter {

  IRI getIdentifier();

  String getName();

  String handle(String value, String query);

}
